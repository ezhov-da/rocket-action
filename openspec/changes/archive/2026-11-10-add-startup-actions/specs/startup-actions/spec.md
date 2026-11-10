# Spec Delta

## Purpose

Lets users automatically run an ordered set of AtomicAction and ChainAction entries when the application starts, with optional input, per-entry enablement, a configurable error policy, and visible run results.

## ADDED Requirements

### Requirement: Ordered startup list

The system SHALL persist a single ordered list of startup entries. Each entry SHALL reference an AtomicAction or ChainAction and SHALL have an enabled flag. The order of entries SHALL define execution order.

#### Scenario: Entries run in list order

- **WHEN** the saved startup list contains entries A, B, C in that order and all are enabled
- **THEN** the system executes A, then B, then C

#### Scenario: Disabled entries are skipped

- **WHEN** an entry is disabled
- **THEN** the system does not execute it and records it as skipped

### Requirement: Startup execution timing

The system SHALL execute enabled startup entries after the main application window becomes visible. Execution MUST run outside the UI event thread and MUST NOT block the main window.

#### Scenario: Main window appears before actions run

- **WHEN** the application starts
- **THEN** the main window becomes visible before the startup list begins execution

#### Scenario: Long-running startup action

- **WHEN** a startup action takes a long time to complete
- **THEN** the main window remains responsive

### Requirement: Execution snapshot

The system SHALL execute a snapshot of the saved startup list. Edits made after a run starts SHALL apply only to subsequent runs.

#### Scenario: Edit during a run

- **WHEN** the user changes the saved startup list while a run is in progress
- **THEN** the current run continues with the snapshot taken at its start and the next run uses the updated list

### Requirement: Per-entry input

The system SHALL pass a configured input value only to actions that accept input. For a chain, input acceptance SHALL be determined by the first AtomicAction in the chain. Input configured for an action that does not accept input SHALL be ignored.

#### Scenario: Atomic action with input contract

- **WHEN** a startup entry references an AtomicAction whose contract accepts input and an input value is configured
- **THEN** the system passes that value to the action

#### Scenario: Action without input contract

- **WHEN** a startup entry references an action that does not accept input
- **THEN** the Startup window disables the input field for that entry and the configured value is ignored

#### Scenario: Chain input

- **WHEN** a startup entry references a chain whose first AtomicAction accepts input
- **THEN** the system passes the configured input to the first action of the chain

### Requirement: Error policy

The system SHALL provide a global stop-on-error setting for the startup list. When the setting is disabled, an execution failure SHALL be recorded and the next enabled entry SHALL run. When enabled, the first execution failure SHALL stop the run and remaining entries SHALL be marked skipped.

#### Scenario: Continue after failure

- **WHEN** stop-on-error is disabled and an entry fails during execution
- **THEN** the system records the failure and continues with the next enabled entry

#### Scenario: Stop after failure

- **WHEN** stop-on-error is enabled and an entry fails during execution
- **THEN** the system stops the run and marks remaining entries as skipped

### Requirement: Missing action handling

A startup entry whose referenced action no longer exists SHALL be treated as a configuration warning. The system SHALL record the warning, skip the entry, and SHALL NOT count it as an execution failure for the stop-on-error setting.

#### Scenario: Missing action with stop-on-error enabled

- **WHEN** an entry references a missing action and stop-on-error is enabled
- **THEN** the system skips the entry with a warning and continues with the next entry

### Requirement: Startup window

The system SHALL provide a separate Startup window that displays the startup entries in execution order and allows the user to add, remove, reorder, edit, enable or disable entries, and save the list. The window SHALL provide controls for the global stop-on-error setting.

#### Scenario: Edit and save

- **WHEN** the user adds, reorders, or edits entries and saves
- **THEN** the system persists the new order and values and uses them for subsequent runs

#### Scenario: Unsaved changes

- **WHEN** the user has unsaved changes in the Startup window
- **THEN** the window indicates that changes are not saved

### Requirement: Blocking during execution

While a startup run is in progress, the Startup window SHALL disable editing controls and SHALL display run progress. The rest of the application SHALL remain usable. The window SHALL re-enable editing when the run finishes, including when the run ends with an unexpected failure.

#### Scenario: Window opened during a run

- **WHEN** the user opens the Startup window while a startup run is in progress
- **THEN** the window opens in read-only mode with progress visible

#### Scenario: Run finishes

- **WHEN** a startup run finishes
- **THEN** the Startup window re-enables editing and shows the run result

#### Scenario: Unexpected failure

- **WHEN** a startup run ends with an unexpected error
- **THEN** the Startup window still re-enables editing

### Requirement: Run now

The Startup window SHALL provide a Run now action that executes only the saved startup list. If unsaved changes exist, the system SHALL offer to save and run or cancel. Run now SHALL be unavailable while another run is in progress or the saved list is empty. Manual runs SHALL be distinguishable from startup runs in the run history.

#### Scenario: Manual run with saved list

- **WHEN** the list has no unsaved changes and the user activates Run now
- **THEN** the system executes the saved list and records the run as manual

#### Scenario: Manual run with unsaved changes

- **WHEN** the list has unsaved changes and the user activates Run now
- **THEN** the system offers to save and run or cancel, and does not start the run unless the save succeeds

#### Scenario: Run now unavailable

- **WHEN** a run is already in progress or the saved list is empty
- **THEN** Run now is unavailable

### Requirement: Run results and log

The system SHALL record a result for each entry in the last run and SHALL display the overall result in the Startup window. The system SHALL persist a run log to disk. The in-memory last-run result MAY reset when the application restarts; the persisted log SHALL remain.

#### Scenario: Per-entry results

- **WHEN** a run finishes
- **THEN** the Startup window shows success, error, skipped, or not-run state for each entry and the overall outcome

#### Scenario: Log after restart

- **WHEN** the application restarts after a previous run
- **THEN** the previous run log remains available on disk

### Requirement: Persistence failure handling

The system SHALL store the startup list in `.rocket-action/startup-actions.json`. If the file is absent, the system SHALL start with an empty list. If the file is unreadable or malformed, the system SHALL log an error, start with an empty list, and continue application startup.

#### Scenario: Missing file

- **WHEN** the startup list file does not exist
- **THEN** the application starts with an empty startup list

#### Scenario: Malformed file

- **WHEN** the startup list file cannot be parsed
- **THEN** the system logs an error, starts with an empty startup list, and the application continues to start

### Requirement: Cleanup on action deletion

When a referenced AtomicAction or ChainAction is deleted, the system SHALL remove its startup entries.

#### Scenario: Delete referenced action

- **WHEN** the user deletes an AtomicAction or ChainAction referenced by startup entries
- **THEN** the system removes those entries from the startup list

### Requirement: Automatic execution kill switch

The system SHALL provide the `rocket.action.startup-actions.enabled` configuration property. When set to false, the system SHALL NOT execute startup entries automatically at application start. The saved list SHALL remain intact, and manual Run now SHALL remain available.

#### Scenario: Automatic execution disabled

- **WHEN** `rocket.action.startup-actions.enabled` is false
- **THEN** the system does not run the startup list at application start and the saved list is preserved

#### Scenario: Manual run still available

- **WHEN** automatic execution is disabled and the user activates Run now
- **THEN** the saved list is executed manually
