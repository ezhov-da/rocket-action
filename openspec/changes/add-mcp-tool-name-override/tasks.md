# Tasks

## 1. Model and persistence

- [x] 1.1 Add `mcpToolName: String? = null` to `AtomicAction` and `ChainAction`, expose it through `Action.mcpToolName()` with a `null` default, and copy it in `duplicate()`; verify with a unit test asserting `duplicate()` keeps the custom name.
- [x] 1.2 Add `mcpToolName: String? = null` to `AtomicActionDto` and `ChainActionDto` and map it in `JsonAtomicActionRepository` and `JsonChainActionRepository`; verify a save/read round-trip unit test keeps the value.
- [x] 1.3 Extend `AtomicActionDtoTest` and `ChainActionDtoTest` with a legacy-JSON case that has no `mcpToolName` property and asserts it reads as `null`; verify with `.\gradlew.bat :application-ui-swing:test --tests "*ActionDtoTest*"`.

## 2. Tool name resolution and MCP server

- [x] 2.1 Extract `translit` from `McpServerService` into a new `McpToolName` object and add `normalize`, `generated` and `resolve(custom, id, name)` there; verify unit tests cover fallback on blank input, Cyrillic transliteration, invalid-character replacement and the 128-character limit.
- [x] 2.2 Add `conflict(candidate, actions, exceptId)` to `McpToolName` and unit-test that it reports another action with the same resolved name, ignores the edited action's own ID and compares generated and custom names alike; verify with the new `McpToolNameTest`.
- [x] 2.3 Add a computed `toolName` to `McpAction` and a `published(actions)` helper in `McpToolName` that keeps the first occurrence of each resolved name and logs a warning for the rest; verify with a unit test on a hand-built list of `McpAction`s.
- [x] 2.4 Switch `McpServerService` to the resolved `McpAction.toolName` and register only `McpToolName.published(...)`; verify existing tests pass with `.\gradlew.bat :application-ui-swing:test --tests "*Mcp*" --tests "*WebServerIntegrationTest*"`.

## 3. Atomic action dialog

- [x] 3.1 Add the "MCP tool name" label and `JTextField` next to the "Use for MCP" checkbox, toggle its `isEnabled` from the checkbox listener, clear it in `showCreateDialog` and load it in `showEditDialog`; verify by building and checking the field is disabled until the checkbox is ticked.
- [x] 3.2 Pass `ChainActionService` into `CreateAndEditAtomicActionDialog` (and its wiring in `ActionsConfigurationPanel` and the dialog test harnesses) and reject a save whose resolved name conflicts with another published atomic action or chain, showing a message with the conflicting action; verify by running the dialog and trying to save a duplicate name.

## 4. Chain action dialog

- [x] 4.1 Add the "MCP tool name" label and `JTextField` next to the "Use for MCP" checkbox, toggle its `isEnabled` from the checkbox listener, clear it in both create entry points and load it in `showEditDialog`; verify by building and checking the field is disabled until the checkbox is ticked.
- [x] 4.2 Reject a save whose resolved name conflicts with another published atomic action or chain, showing a message with the conflicting action; verify by running the dialog and trying to save a duplicate name.

## 5. Verification

- [x] 5.1 Run `.\gradlew.bat :application-ui-swing:test` and confirm the whole module test suite passes.
- [x] 5.2 Start the application with the existing `.rocket-action` data, mark an action with a custom Cyrillic name and call `tools/list` on `POST /mcp`; verify the tool appears under the normalized custom name while untouched actions keep their previous generated names.

## Workflow follow-up

- Archive the change with `/opsx-archive` after review.
