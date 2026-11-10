# Design

## Context

See `proposal.md` for motivation. The current application has one automatic execution path: `ActionScheduler` stores an optional Quartz cron expression per AtomicAction or ChainAction, and `ActionSchedulerRunnerService` registers those cron jobs during bean initialization.

Relevant constraints from the existing code:

- `AtomicAction` and `ChainAction` are persisted as JSON under `.rocket-action/`.
- `ActionExecutorService.execute(input, actionId, progress)` resolves the action and delegates to the synchronous `ActionExecutor`.
- `ActionExecutorImpl` passes the external input only to the first action of a chain; later chain steps receive the previous step's result.
- The application context starts inside `SwingUtilities.invokeLater`, and `AppRunService.run` shows the main window afterwards.
- The scheduler UI is available only for actions whose first contract accepts no input (`isUnitInputContract()`).
- Swing UI state is updated from background work through `SwingUtilities.invokeLater`; existing UI panels subscribe to domain events.

## Goals / Non-Goals

**Goals:**

- Persist one ordered startup list for AtomicAction and ChainAction.
- Support per-entry input, per-entry enablement, and a global stop-on-error policy.
- Run the list after the main window is visible, off the UI event thread, using a saved snapshot.
- Provide a separate Startup window with editing, last-run results, and a saved-list-only `Run now`.
- Block only the Startup window while a run is active.
- Keep run history in a log file and allow a configuration kill switch.

**Non-Goals:**

- Unifying cron and startup triggers into one trigger model.
- Multiple startup profiles or named lists.
- Starting actions before the main window is visible.
- Cancelling an in-flight action; `ActionExecutor` has no cooperative cancellation.
- Persisting last-run status across application restarts.
- Running legacy quick actions from `actions.yml` at startup.
- Changing the plugin API, HTTP API, MCP endpoint, or action file format.

## Decisions

### Separate startup aggregate instead of extending the scheduler

Startup configuration is a new aggregate: a `StartupActions` root with an ordered list of entries and a `stopOnError` flag. It is not stored in `ActionScheduler`.

Rationale: the existing scheduler is cron-centric, stores one file per action, and cannot express ordering, duplicate runs, or per-entry input. Keeping startup separate avoids overloading `ActionScheduler` and its UI.

Alternatives considered:

- `startup` flag in `ActionScheduler`: smallest change, but no ordering, duplicates, or input, and it mixes two trigger semantics.
- Startup ChainAction flag: reuses chain ordering, but requires wrapping AtomicActions, passes results between independent entries, and stops the whole chain on failure.

### JSON storage with one ordered file

The list is stored in `.rocket-action/startup-actions.json`:

```json
{
  "stopOnError": false,
  "startupActions": [
    {
      "id": "entry-uuid",
      "actionId": "action-uuid",
      "input": null,
      "enabled": true
    }
  ]
}
```

Array order is execution order. `id` identifies an entry for result reporting and logging, and allows the same action to appear multiple times. A malformed or missing file is treated as an empty list and logged; it must not prevent application startup.

A run log is appended to `.rocket-action/startup-actions/log.txt`. In-memory last-run results reset on restart; the log persists.

Alternative considered: YAML for easier manual editing. JSON is chosen for consistency with the existing `.rocket-action` state and because the UI is the primary editor.

### Execution hook after the main window is visible

`AppRunService.run` calls a new `StartupActionRunner.startAsync(ON_STARTUP)` after `baseDialogFactory.dialog.isVisible = true`.

Rationale: the main window appears immediately; a long startup action cannot delay it. The hook is also test-friendly: context tests create the Spring context but do not call `AppRunService.run`, so real startup actions are not triggered.

Alternatives considered:

- `ApplicationReadyEvent` or `InitializingBean`: runs before the UI is shown and would delay visual startup.
- Quartz one-shot trigger: reuses scheduler infrastructure but introduces cron terminology and lifecycle coupling for a different trigger.

### Sequential execution with snapshot isolation

The runner takes a snapshot of the saved list, filters enabled entries, and executes them sequentially in array order. Edits made after the run starts apply to the next run.

Rationale: deterministic order, simple failure semantics, and no unexpected concurrency in Groovy actions or shared variables. Parallel execution is not needed for the described use cases.

### Input acceptance follows action contracts

`input` is passed only when the target accepts input:

- AtomicAction: its `ContractType` input side is `IN`.
- ChainAction: the first AtomicAction in the chain has an `IN` input contract.

This mirrors `ActionExecutorImpl`: external input reaches only the first chain step. If the first step is missing or the chain is empty, input acceptance cannot be determined; the field is disabled and the entry is treated as unresolved at execution time.

The Startup window disables the input field for entries that do not accept input rather than hiding it, so the reason is visible.

### Execution failures and missing actions are separate concerns

Only an execution failure (an exception or an `onAtomicActionFailure` callback) counts for `stopOnError`.

A missing referenced action is a configuration warning: the entry is skipped, recorded as unresolved/skipped, and the run continues regardless of `stopOnError`. Disabled entries are skipped without being treated as errors. When a referenced AtomicAction or ChainAction is deleted, the service removes its startup entries, consistent with the scheduler cleanup.

### Separate Startup window with a run-state lock

The Startup window is opened from `ChainConfigurationFrame` as a separate frame, next to the existing Schedulers entry point.

The window provides:

- an ordered list with add, remove, up, down, input, enabled, and save controls;
- a global `Stop on first error` toggle;
- `Run now` for the saved list only;
- last-run results per entry and an overall outcome;
- a log-opening action.

While a run is active, all editing controls are disabled; the window shows progress and remains readable. The main window stays usable. If the window is opened during a run, it initializes in the read-only running state. Closing it is allowed; reopening shows the current run state.

`StartupActionRunner` exposes a thread-safe current-run snapshot and last-run result. The window queries that state on open and subscribes to run lifecycle domain events. The runner always publishes the finish event in a `finally` block so an unexpected error cannot leave the UI locked.

### Run now uses the saved list

`Run now` is enabled when the saved list is non-empty and no run is active. If the editor has unsaved changes, it shows `Save and run` / `Cancel`. The run starts only after a successful save. Manual runs are recorded with trigger `MANUAL`; automatic runs use `STARTUP`.

Rationale: the file and the executed list always match, and there is no hidden in-memory configuration. Alternative approaches (run unsaved state, auto-save) were rejected as less predictable.

### Kill switch property

`rocket.action.startup-actions.enabled` (default `true`) disables automatic startup execution when false. The saved list remains intact and manual `Run now` remains available. This supports tests, troubleshooting, and users who want to launch the app without the startup routine.

## Architecture

```text
AppRunService.run()
  |
  +-> main window visible
  +-> StartupActionRunner.startAsync(ON_STARTUP)

StartupActionsFrame
  |
  +-> StartupActionService (read/save, cleanup, validation)
  +-> StartupActionRunner.startAsync(MANUAL)

StartupActionRunner
  |
  +-> StartupActionsRepository (snapshot)
  +-> ActionExecutorService
  +-> StartupActionLogRepository
  +-> domain events (run started/entry started/entry finished/run finished)
```

Proposed package layout under `ru.ezhov.rocket.action.application.chainaction.startup`:

- `domain.model`: startup entry, startup list, run status, run result, run state.
- `domain`: repository contracts, run lifecycle events.
- `application`: `StartupActionService`, `StartupActionRunner`.
- `infrastructure`: JSON repository and file log repository.
- `interfaces.ui`: Startup frame, panel, table model, dialogs.

Existing files to touch:

- `AppRunService`: inject the runner and start the automatic run after the window is visible.
- `ChainConfigurationFrame` and its construction site: add the Startup entry point and pass dependencies.
- `UsedPropertiesName`: document `rocket.action.startup-actions.enabled`.
- `README.md`: document the feature and file layout.
- Tests and manual harnesses for the new repository, runner, and UI.

## Execution Flow

```text
start(trigger)
  -> atomically enter RUNNING; reject if already running
  -> load saved snapshot; filter enabled; build run result
  -> for each entry in order:
       -> resolve AtomicAction or ChainAction
       -> missing? mark UNRESOLVED / SKIPPED, log warning, continue
       -> determine input acceptance; pass input only if accepted
       -> execute and update result on success/failure
       -> execution failure and stopOnError? mark remaining SKIPPED, break
  -> finally: publish run finished, store last-run result, enter IDLE
```

All UI updates from the runner go through `SwingUtilities.invokeLater`. The runner is the only component allowed to start a run, and the `AtomicBoolean` guard makes concurrent automatic/manual starts impossible.

## Risks / Trade-offs

- **Hung action keeps the Startup window locked** -> there is no cancellation in v1; the window shows progress and remains readable. A later change can add cooperative cancellation or a timeout.
- **Startup actions have side effects on every launch** -> the list is user-managed, visible in the UI, and can be disabled with the kill switch; no confirmation is added because automatic execution is the feature's purpose.
- **Last-run result is lost on restart** -> the log persists; status persistence can be added later as a separate file.
- **External edits during a run** -> the runner uses a snapshot, so the current run is unaffected; the UI lock applies only to the application window, not to manual JSON edits.
- **Malformed startup file** -> the application starts with an empty list and logs the error; the user must fix the file or recreate the list in the UI.
- **Cleanup removes entries on action deletion** -> recreating an action does not restore its startup entries; the user must re-add them.
- **Log growth** -> the log is append-only and has no rotation in v1, matching the existing scheduler log behavior.
- **Test contamination** -> the hook lives in `AppRunService.run`, which tests do not call; the kill switch provides an additional guard for manual harnesses.

## Migration Plan

- Add the new startup package, repository, runner, UI, and properties; no existing scheduler data is migrated.
- Existing installations have no `.rocket-action/startup-actions.json`; the feature is a no-op until entries are added.
- Rollback is removing the new UI entry point and runner call; the JSON and log files are inert and can be left in place.
- Documentation and `UsedPropertiesName` entries are updated together with the feature.

## Open Questions

None. Deferred follow-ups (cancellation, persistent status, log rotation) are listed as risks and do not change the current specifications or task breakdown.
