# Tasks

## 1. Repository read caches

- [x] 1.1 Add an in-memory `allCached` list to `JsonAtomicActionRepository`, invalidated in `save` and `delete`, and verify with a unit test that two consecutive `all()` calls parse the file once and that `save`/`delete` make the next `all()` return fresh content
- [x] 1.2 Add an in-memory snapshot plus `actionId -> ActionScheduler` index to `JsonFileActionSchedulerRepository`, invalidate both in `save`/`delete`, and remove directory creation from the read path; verify with a test that repeated `all()` calls read the folder once and that `save`/`delete` refresh the index
- [x] 1.3 Make `ActionSchedulerService.scheduler(id)` read from the scheduler index without building the full chains + atomics map; verify with a test that decorating a non-scheduled action does not load chains or atomics (mock verification) and that the returned DTO still contains action, scheduler, and log file
- [x] 1.4 Document in `README.md` that scheduler/action JSON files edited outside the running application are re-read only after restart, and verify the documented statement matches the implemented invalidation points

## 2. Row decoration snapshot and renderers

- [x] 2.1 Introduce `ActionRowDecoration` (display title HTML, scheduler status, optional usage count) and an immutable snapshot keyed by action id, with a builder that computes it from already-loaded chains, atomics, and scheduler entries; verify with a unit test that usage counts and scheduler statuses match the source data
- [x] 2.2 Rework `AtomicActionListCellPanel` and `ChainActionListCellPanel` to take decoration from the snapshot instead of calling services, and stop calling `Markdown` while building a cell; verify with a test whose mocked services throw on any call and assert that a cell still renders
- [x] 2.3 Update `AtomicActionListCellRenderer`, `ChainActionListCellRenderer`, `OrderAtomicActionListCellRenderer`, and `ChainAndAtomicActionListCellRenderer` to read only the snapshot; verify with a test that renders one cell per action type with mocked services and asserts zero service interactions
- [x] 2.4 Build the CommonMark `Parser` and `HtmlRenderer` once inside `Markdown` and reuse them for every conversion; verify with the existing Markdown tests plus a test that two conversions produce identical output and do not rebuild the parser

## 3. Chains and Actions editor panels

- [x] 3.1 Set `fixedCellHeight` for the `allListChains` and `allListActions` lists, measured from a prototype cell with the active UI font; verify with a test that `getPreferredSize()` on a list holding 1000 elements performs zero renderer calls
- [x] 3.2 Add a generation counter and cancellation to the `SearchWorker` of `ActionsConfigurationPanel` and `ChainsConfigurationPanel`, and replace `removeAllElements()` + per-element `addElement()` with a detached model built in `done()` and installed via `setModel`; verify with a test that a slow first search followed by a fast second applies only the second result and that a refill fires exactly one model change event
- [x] 3.3 Rebuild the decoration snapshot in the domain-event handlers of both panels before repainting, keeping the existing scheduler-filter behavior; verify with a test that an action update event changes the rendered name and that a scheduler event changes the decoration without re-reading repositories
- [x] 3.4 Keep the existing search/filter output in both panels and add regression tests covering atomic text search (id, name, description, data, alias, keyboard-layout transformation), chain text search (id, name, description), sort fields, and condition filters

## 4. Action-selection popup and chain editor

- [x] 4.1 In `SelectChainListPanel`, set a fixed cell height, consume the decoration snapshot, reuse the latest-wins worker pattern, and make the clear button refill the list with empty text; verify with a test that clearing after a search restores all eligible chains and atomic actions
- [x] 4.2 In `SelectChainPopupMenu` and `SelectChainButtonsPanel`, pass the snapshot to the list and button panels and keep a fixed list height when the "Show buttons" count changes; verify with a test that rebuilding the popup for a different button count keeps the full action set available in the list
- [x] 4.3 Move `CreateAndEditChainActionDialog.fillList()` filtering and repository reads into a cancellable worker, install the result with a single model swap, and set fixed cell heights for the available-actions and selected-actions lists; verify with a test that no repository access happens on the event thread during a refresh and that a stale refresh does not overwrite a newer one
- [x] 4.4 Apply the fixed height and decoration snapshot to `ChainsSelectPanel` (usage popup); verify with a test that the usage list renders the scheduler decoration from the snapshot without calling services

## 5. Integration check

- [x] 5.1 Add a performance smoke test with 1000 atomic actions (each with a large script body) and 100 scheduler records that performs a search, a reset, and one layout pass, asserting a bounded duration and renderer calls limited to visible rows
- [x] 5.2 Run `.\gradlew.bat :application-ui-swing:test` with JDK 17 and confirm all new and existing tests pass
- [x] 5.3 Manually verify on a build with a large local profile that search and reset in the Chains editor, the action-selection popup, and the chain editor stay responsive and that lists and scheduler decorations still match the saved data

## Workflow follow-up

- Archive the change after review and a green test run.
- Verify the archived `action-browser` spec appears under `openspec/specs/`.
