# Proposal

## Why

Searching and resetting AtomicAction and ChainAction lists freezes the UI for several seconds. On every refill the `JList` model is rebuilt, and because `fixedCellHeight` is not set, Swing's `BasicListUI.updateLayoutState()` renders **every** model element to measure it. Each cell render in turn performs disk I/O: `JsonAtomicActionRepository.all()` re-reads and re-parses the whole `atomic-actions.json`, and `JsonFileActionSchedulerRepository.all()` lists and parses every scheduler file — once per cell, per repaint. The cost grows with the number of actions, action script size, and the number of schedulers, so the freeze appears "sometimes" and only on search/reset, not while scrolling.

## What Changes

- Stop rendering the whole model on list refill:
  - set a fixed row height for the action and chain lists (editor panels and the action-selection popup);
  - replace `removeAllElements()` + `N x addElement()` with a single bulk model update so one layout pass replaces `N` model events.
- Remove I/O and heavy work from cell rendering:
  - add a read cache to `JsonAtomicActionRepository` (invalidated on save/delete), mirroring `JsonChainActionRepository`;
  - add an in-memory index/cache to `JsonFileActionSchedulerRepository` and serve `ActionSchedulerService.scheduler(id)` from it without walking all actions;
  - render scheduler status, usage count, and display markup from a snapshot computed in the background search worker instead of querying services inside `getListCellRendererComponent`.
- Reuse a single CommonMark parser/renderer instead of building them on every `Markdown.textMarkdownToHtml` call, and avoid parsing Markdown per cell render.
- Cancel the previous search worker before starting a new one; stale results must not rebuild the list.
- Make the reset (X) button in the action-selection popup actually restore the unfiltered list (currently it only clears the text field).

## Capabilities

### New Capabilities
- `action-browser`: behavior of the UI that lists, searches, resets, and renders AtomicAction and ChainAction lists (Chains editor panels, action-selection popup, chain editor action lists), including responsiveness, rendering purity, reset semantics, and cache freshness.

### Modified Capabilities
<!-- No existing capability changes: mcp-tools and startup-actions requirements are unaffected. -->

## Impact

- UI: `ActionsConfigurationPanel`, `ChainsConfigurationPanel`, `SelectChainListPanel`, the `*ListCellRenderer` implementations, `AtomicActionListCellPanel` / `ChainActionListCellPanel`, `SelectChainPopupMenu`.
- Infrastructure: `JsonAtomicActionRepository`, `JsonFileActionSchedulerRepository`, `ActionSchedulerService`, `FileActionSchedulerLogRepository` (read path only), `Markdown`.
- No API, file-format, or persistence changes; existing `.rocket-action/*.json` files stay compatible.
- Tests: repository cache invalidation, renderer purity, reset behavior, and worker cancellation.
