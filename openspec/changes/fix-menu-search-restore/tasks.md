# Tasks

## 1. Placement snapshot and menu assembly

- [x] 1.1 Add a `MenuStructure` snapshot (roots, container ids, ordered child ids) built from the `ActionsModel` already read by `RocketActionSettingsService.loadAndGetAllComponents`; verify with a unit test that a fixture with nested groups keeps ids and order and that the model is not retained
- [x] 1.2 Add a recursive assembly that clears each group container and re-attaches its children from the cache in order; verify a unit test with one-, two- and three-level nesting returns attached components in catalog order
- [x] 1.3 Use the assembly for the final component resolution in `loadAndGetAllComponents` instead of the top-level-only loop; verify `RocketActionSettingsServiceReloadTest` still passes, including `should recreate a group only when its child was rebuilt` and the `created`/`evaluations` counters unchanged
- [x] 1.4 Replace `getAllExistsComponents` with the structure-driven restore; verify a test that simulates a search (moves a nested child to the root menu) then restores and asserts the child is back in its group with the group's full child count
- [x] 1.5 Guard the assembly against cyclic references (`A` contains `B`, `B` contains `A`) and duplicate ids; verify a test asserts no stack overflow, one warning per rebuild, and that a duplicate is placed once
- [x] 1.6 Add a reload-after-search case to the new `RocketActionSettingsServiceRestoreTest` asserting that a full reload after a search produces the complete tree with no missing child (Refresh as a repair path)

## 2. Search and reset in the UI

- [x] 2.1 Apply the restore on the EDT for the Clear button, Esc and Ctrl+Enter with an empty field; verify `UiQuickActionServiceReloadTest` (or a new case) shows the original structure after reset and that the search field text/background are reset
- [x] 2.2 Route the tags-menu search through the same restore path; verify a test with a tagged nested entry returns that entry to its group after reset
- [x] 2.3 Keep the final attach in one EDT batch inside `swapContent` for both Create and Restore, with the previous content visible until the swap; verify the existing `UiQuickActionServiceReloadTest` cases (`should keep the chrome…`, `should keep the typed text…`) still pass and `swap` is still a single batch
- [x] 2.4 Verify repeated searches do not lose entries: a test runs two searches in a row, resets once, and asserts every entry is present in its group

## 3. Integration and documentation

- [x] 3.1 Run `.\gradlew.bat :application-ui-swing:test` and confirm the menu/reload/group suites are green with no skipped new tests
- [x] 3.2 Check the reference catalog manually: search for an entry nested two groups deep, clear the search, refresh after a search, and confirm the menu structure/order and the warm reload time are unchanged (record the metrics line in the change folder)
- [x] 3.3 Document in `AGENTS.md` that the menu content is assembled from the placement snapshot and that the cached Swing components must never be used as the source of structure on restore; verify the wording matches the final code

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Re-check `openspec validate fix-menu-search-restore` before archiving.
