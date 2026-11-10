# Proposal

## Why

Rocket Action can schedule AtomicAction and ChainAction execution only with Quartz cron expressions. Users who need a repeatable preparation routine — for example, opening a workspace, syncing data, or starting external tools — must either wait for a cron tick or run the actions manually after every launch. There is no way to say "run these actions when the application starts".

## What Changes

- Add a dedicated, ordered **startup actions** list for AtomicAction and ChainAction.
- Each entry stores the action reference, an optional input, and an enabled flag; the list order defines execution order.
- Add a global `stopOnError` setting: on an execution failure, either continue with the remaining entries or stop the list.
- Persist the list in `.rocket-action/startup-actions.json` and write run history to `.rocket-action/startup-actions/log.txt`.
- Execute the list after the main window becomes visible, on a background thread, using a snapshot of the saved list. Startup execution must never block the UI.
- Add a separate **Startup** window with the ordered list editor, input field (only for actions that accept input), `enabled` toggle, `stopOnError` toggle, last-run results, and a **Run now** button.
- `Run now` executes only the saved list. If the editor has unsaved changes, offer `Save and run` / `Cancel`.
- While a startup run is in progress, block only the Startup window; the rest of the application remains usable.
- Show `input` only for actions whose contract accepts input. For a chain, resolve the first AtomicAction in the chain.
- Treat a missing action as a configuration warning and skip it; only an execution failure counts for `stopOnError`.
- Remove startup entries when the referenced AtomicAction or ChainAction is deleted.
- Add the `rocket.action.startup-actions.enabled` property as a troubleshooting and test kill switch.

## Capabilities

### New Capabilities
- `startup-actions`: ordered, persisted startup execution of AtomicAction and ChainAction, including input handling, error policy, run observability, manual run, and UI behavior.

### Modified Capabilities
- None. No existing capability specifications exist in this repository.

## Impact

- `application-ui-swing` main application code, primarily the chain-action subsystem.
- New startup domain, application, infrastructure, and Swing UI packages alongside the existing scheduler.
- `AppRunService` gains a non-blocking launch hook after the main window is shown.
- `ChainConfigurationFrame` gains an entry point to the new Startup window.
- New runtime data under `.rocket-action/startup-actions/`; no changes to existing scheduler files.
- New property in `UsedPropertiesName` and related configuration documentation.
- No changes to the plugin API, HTTP API, MCP endpoint, or action file format.
- Tests must keep Spring context tests from triggering real startup actions.
