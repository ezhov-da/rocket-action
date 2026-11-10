# Tasks

## 1. Domain and persistence

- [x] 1.1 Add the startup aggregate model (`StartupActions`, `StartupActionEntry`, run status, run state, run result) under `chainaction.startup.domain.model`; verify with a unit test covering entry identity, array order, and result statuses.
- [x] 1.2 Add `StartupActionsRepository` and `JsonStartupActionsRepository` using `.rocket-action/startup-actions.json`, with missing-file fallback and malformed-file logging; verify with repository tests for round-trip, missing file, and malformed file.
- [x] 1.3 Add `StartupActionLogRepository` and a file implementation writing `.rocket-action/startup-actions/log.txt`, including trigger source (`STARTUP` / `MANUAL`) and per-entry outcomes; verify with a test that appends and reads run entries.

## 2. Application logic

- [x] 2.1 Implement `StartupActionService` for load/save, add/remove/reorder, enabled toggling, unresolved detection, and input acceptance (AtomicAction contract; ChainAction first AtomicAction); verify with service tests covering both action types.
- [x] 2.2 Subscribe to `AtomicActionDeletedDomainEvent` and `ChainActionDeletedDomainEvent` and remove matching startup entries; verify with cleanup tests.
- [x] 2.3 Implement `StartupActionRunner` with snapshot execution, enabled filtering, sequential order, input passing, status recording, `stopOnError`, missing-action warning, single-run guard, trigger source, lifecycle events, and finish publication in `finally`; verify with runner tests covering success, failure, skip, stop, and unexpected error.
- [x] 2.4 Add `rocket.action.startup-actions.enabled` to `UsedPropertiesName` and skip automatic execution when false; verify with a property test and a runner test showing the saved list remains available.

## 3. Execution hook

- [x] 3.1 Inject `StartupActionRunner` into `AppRunService` and start `ON_STARTUP` after `baseDialogFactory.dialog.isVisible = true`; verify with a focused test using a mock dialog that the runner is invoked after visibility.
- [x] 3.2 Confirm Spring context tests and manual harnesses do not trigger startup actions; verify `.\gradlew.bat :application-ui-swing:test` passes without startup side effects.

## 4. Startup UI

- [x] 4.1 Create the separate Startup window with an ordered table, Add/Remove/Up/Down/Save, input, and enabled controls; verify with a UI model test or manual harness that saves and reloads the JSON list.
- [x] 4.2 Add the action picker for AtomicAction and ChainAction and enable input only when the action accepts it; verify with UI model tests for an atomic `IN` action, an atomic `UNIT` action, and a chain whose first action is `IN`.
- [x] 4.3 Add the `Stop on first error` toggle, dirty tracking, explicit Save, and an unsaved-changes indicator; verify with UI model tests.
- [x] 4.4 Add run-state subscription: disable editing during `RUNNING`, show progress, per-entry results, last-run summary, and a log-opening action; verify with UI state tests for opening during a run and re-enabling after finish and after an unexpected error.
- [x] 4.5 Add `Run now` for the saved list only, unavailable when the list is empty or a run is active, with `Save and run` / `Cancel` when dirty and `MANUAL` trigger recording; verify with UI tests and a manual harness.
- [x] 4.6 Add the Startup entry point next to Schedulers in `ChainConfigurationFrame`; verify by launching the application and opening both windows.

## 5. Documentation

- [x] 5.1 Document the startup list, JSON shape, `stopOnError`, `Run now`, window locking, log location, and kill switch in `README.md`; verify the documented paths and examples match the implementation.
- [x] 5.2 Document `rocket.action.startup-actions.enabled` in `UsedPropertiesName` and the available-properties UI; verify the property appears with a useful description.

## 6. Integration verification

- [x] 6.1 Run `.\gradlew.bat :application-ui-swing:test` and verify the test suite passes.
- [x] 6.2 Run the application manually, add one AtomicAction and one ChainAction to the startup list, use `Run now`, restart the application, and verify order, input, stop-on-error, log output, and Startup-window locking.
- [x] 6.3 Run `openspec validate add-startup-actions --strict` and verify the change validates without errors.

## Workflow follow-up

- Archive the change after implementation and review are complete.
- Verify the archived `startup-actions` capability and that the change directory is closed.
