# Design

## Context

See `proposal.md` for motivation. Current state that shapes the approach:

- Four Swing lists render AtomicAction/ChainAction rows with `DefaultListCellRenderer` subclasses that build a `JPanel` with `MigLayout` and labels on **every** call: `AtomicActionListCellRenderer`, `ChainActionListCellRenderer`, `OrderAtomicActionListCellRenderer`, `ChainAtomicActionListCellRenderer` (used by the action-selection popup). Renderers call `chainIcon()` (2 `AtomicActionService.atomicBy()` lookups), `ChainActionService.usageAction()`, `ActionSchedulerService.scheduler()`, `Markdown.textMarkdownToHtml()`.
- `JList` has no fixed cell height. In JDK 17 `BasicListUI.updateLayoutState()` iterates the entire model and calls the renderer plus `getPreferredSize()` whenever `fixedCellWidth == -1 || fixedCellHeight == -1` (`javax/swing/plaf/basic/BasicListUI.java:1435-1453`). Every model change therefore costs `O(N)` renderer calls, and each call currently performs file I/O.
- `JsonAtomicActionRepository.all()` reads and parses `atomic-actions.json` on every call; `JsonChainActionRepository` already caches in `allCached` and invalidates on save.
- `JsonFileActionSchedulerRepository.all()` lists the scheduler folder and parses every scheduler JSON on every call; `ActionSchedulerService.scheduler(id)` uses it per cell and additionally builds `getActions()` (chains + atomics) for scheduled entries. `FileActionSchedulerLogRepository.get()` performs a file existence check.
- `Markdown.markdownToHtml()` constructs a CommonMark `Parser` and `HtmlRenderer` per call.
- Two panels refill lists in a `SwingWorker` (`ActionsConfigurationPanel`, `ChainsConfigurationPanel`), but `done()` clears the model and adds elements one by one; neither cancels a previous worker. `CreateAndEditChainActionDialog.fillList()` filters and reads repositories **synchronously on the EDT**. `SelectChainListPanel` uses a worker but its clear button only clears the text field.

Constraints: Java 17, no new libraries, `.rocket-action/*.json` format unchanged, UI is created outside Spring, domain events drive list updates, and `CreateAndEditChainActionDialog` uses `DefaultListModel` through a DnD handler that casts `list.model as DefaultListModel`.

## Goals / Non-Goals

**Goals:**

- Make list layout/repaint cost independent of the number of persisted actions and schedulers.
- Remove all file I/O, Markdown parsing, and cross-service lookups from cell rendering.
- Keep search/reset results, filters, and ordering unchanged.
- Guarantee latest-wins semantics and a working reset in every action list.
- Cover the behavior with unit tests that fail on the current implementation.

**Non-Goals:**

- Redesigning the lists, their toolbars, or keyboard navigation.
- Changing search ranking or which fields are matched.
- Changing persistence formats or introducing external indexes.
- Speeding up the regular-action menu search in `UiQuickActionService` (not an AtomicAction/ChainAction list).

## Decisions

### D1. Fixed row height for every action list

Set `fixedCellHeight` for each `JList` (Chains editor, Actions editor, action-selection popup, chain editor action list, usage popup), derived once from a prototype cell panel's preferred height plus insets, measured with the configured UI font. This makes `BasicListUI.updateLayoutState()` skip the model scan entirely.

Alternatives considered: `setPrototypeCellValue` alone (it only marks layout dirty; heights stay dynamic), fixed width only (height scan still runs), and keeping dynamic heights with a cached renderer (preferred-size computation still runs `O(N)` times per change).

### D2. Immutable row-decoration snapshot built off the EDT

Introduce a small view model, e.g. `ActionRowDecoration(titleHtml: String, schedulerStatus: ActionSchedulerStatus?, usageCount: Int?)`, and an immutable snapshot `Map<String, ActionRowDecoration>` keyed by action id. Each search worker computes the snapshot from already-loaded data (chains, atomics, scheduler entries) and publishes it together with the filtered list. Renderers read only from the snapshot: no service calls, no file reads, no Markdown parsing.

Alternatives considered: caching decoration inside the renderer (first paint still computes on the EDT), and passing full row view models (larger refactor for the same effect).

When a domain event changes data (action created/updated/deleted, scheduler changed), the owning panel rebuilds the in-memory snapshot before repainting; scheduler-status events then only need a repaint.

### D3. Read caches with explicit invalidation

- `JsonAtomicActionRepository`: add `allCached`, invalidated in `save`/`delete`, mirroring `JsonChainActionRepository`.
- `JsonFileActionSchedulerRepository`: keep an in-memory snapshot plus `actionId -> ActionScheduler` index, invalidated in `save`/`delete`; reads must not create directories as a side effect.
- `ActionSchedulerService.scheduler(id)`: serve from the index; do not build the full `getActions()` map for decoration. Keep `all()`/`exists()`/`scheduledActionIds()` behavior for existing callers.

Alternatives considered: checking file timestamps on every read (still I/O), `WatchService` (extra threads and lifecycle), and keeping the status quo (rejected: this is the dominant cost per cell).

### D4. One bulk model swap per refresh

Build a new detached `DefaultListModel` inside `done()` and replace the list model via `setModel`, so a refresh produces a single model-change event and one layout pass. Keep `DefaultListModel` (not a custom `AbstractListModel`) because the chain editor's DnD handler requires it; update the fields that hold the current model accordingly.

Alternatives considered: a custom observable model (breaks the DnD cast), a `DefaultListModel` subclass firing one event (needs protected state), and adding elements individually (current behavior).

### D5. Latest-wins worker lifecycle

Each panel keeps a reference to its current worker and a generation counter. Starting a refresh cancels the previous worker and increments the generation; `done()` applies results only when its generation is still current and the worker was not cancelled. The same pattern is applied to `CreateAndEditChainActionDialog`, whose filtering moves into a worker because it currently reads repositories and lowercases script bodies on the EDT.

### D6. Single Markdown parser/renderer and precomputed titles

Build CommonMark `Parser` and `HtmlRenderer` once (they are reusable and thread-safe after configuration) and expose a helper that converts text to HTML. Row titles are computed in the worker into `ActionRowDecoration.titleHtml`, so painting never parses Markdown.

### D7. Reset always refills

Every reset control clears the text field **and** triggers a refresh with empty text. In `SelectChainListPanel` this means invoking the same `fillList(null)` path as an empty search; the Chains/Actions editor panels already refresh and keep that behavior.

### D8. Snapshot freshness

Caches are refreshed on every successful write through the repositories. Snapshot rebuilds happen on domain events, so lists and decorations reflect saved changes immediately, satisfying the "saved action data is visible immediately" requirement without restarting the application.

## Risks / Trade-offs

- **Clipped row content** → measure the fixed height from a prototype with the active font, keep titles single-line with elision, and add vertical margin; no wrapping in cells.
- **Stale cache if JSON is edited outside the application** → same limitation as today's chain cache; document it and rely on restart to re-read files. All in-app writes invalidate the cache.
- **Snapshot lag after a domain event** → rebuild the snapshot in the event handler before repaint; the rebuild is in-memory and `O(actions + chains + schedulers)`.
- **Renderer/paint ordering** → set the new renderer snapshot before swapping the model to avoid painting stale decorations.
- **Thread-safety** → publish immutable snapshots on the EDT, and keep repository caches guarded by the existing locks (the scheduler repository already has one).
- **Regression risk in search semantics** → keep `AtomicActionsFilter`/chain filtering untouched, including keyboard-layout transformation, and add regression tests for the matched sets.

## Migration Plan

No data or configuration migration. Ship as a normal release; the caches are in-memory only, so rolling back to the previous build simply restores the old behavior. Verify on a profile with many AtomicActions containing large scripts and several scheduler files.

## Open Questions

None blocking; implementation details such as the exact fixed height value are resolved during implementation by measuring the prototype cell.
