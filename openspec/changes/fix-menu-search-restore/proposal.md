# Proposal

## Why

After the 0.25.0 reload optimisation, searching the action menu and then clearing the
search does not rebuild the menu: the found entries are not returned to their groups, and
entries nested in a group disappear from the menu permanently. The optimisation replaced
the per-group event subscribers (the previous repair path) with a reload change set, on
the assumption that "group menus are not touched by search". That assumption is wrong:
search re-parents the shared, cached Swing components into the content menu, and a Swing
component has exactly one parent, so the search view physically disassembles the group
trees.

The only workaround today is restarting the application (or forcing the affected group to
be recreated by editing its settings), because unchanged groups are reused from the cache
by design and `Action.Restore` only re-adds the top-level component list.

## What Changes

- Assemble the content menu from an explicit placement snapshot of the last reload
  (top-level ids plus each group's ordered child ids) instead of relying on where the
  shared components currently sit.
- Make the same assembly the restore path for clearing the search (the Clear button, Esc,
  and Ctrl+Enter with an empty field), so every found entry returns to its original group
  at any nesting depth.
- Use the same assembly on a full reload, so a manual Refresh also repairs a menu damaged
  by a search instead of keeping the holes.
- Keep the performance guarantees of the reload: unchanged actions and groups are not
  recreated, settings are not re-evaluated, and no plugin is called on restore. The
  restore is a re-attachment of already built components.
- Guard the assembly against cyclic and duplicate action ids, and perform the Swing
  re-attachment on the event thread.
- No **BREAKING** changes: the actions file, plugin API, HTTP/MCP contracts and the
  visible action order stay as they are.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `action-menu-reload`: adds the requirement that searching the menu and clearing the
  search keep the menu structure intact — a found entry shown in the search view returns
  to its group on restore, repeated searches do not lose entries, and a reload after a
  search produces the complete menu.

## Impact

- `RocketActionSettingsService` (placement snapshot captured from the already read actions
  model; one tree assembly used by both load and restore; `getAllExistsComponents` replaced
  by a structure-driven restore).
- `UiQuickActionService` (`fillMenuByRocketAction`, `resetSearch`, the empty Ctrl+Enter
  branch, `swapContent`/`CreateMenuOrGetExistsWorker`), including moving the Swing
  re-attachment onto the EDT.
- `GroupRocketActionUi` stays the owner of a group's component, but the group no longer
  needs to be recreated to be repaired; the "unchanged group is reused" guarantee and its
  test remain valid.
- Tests: reload/restore tests in `application-ui-swing`, the menu reload UI test, and the
  `action-menu-reload` specification.
- No persisted format, public API, dependency or configuration change.
