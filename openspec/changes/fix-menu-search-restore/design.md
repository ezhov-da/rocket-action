# Design

## Context

See `proposal.md` — Why. The facts that shape the approach:

- `RocketAction.component()` returns one cached Swing instance per action id
  (`RocketActionComponentCache` → `RocketActionCached.origin`). Every plugin stores its
  `JMenu`/`JMenuItem`/`JPanel` in a local and returns it from `component()`.
- `GroupRocketActionUi.create` builds a `JMenu` and adds `cache.by(childId).origin.component()`
  for each child. The group is reused when `isChanged` is false, so its menu is not rebuilt.
- `UiQuickActionService.fillMenuByRocketAction` shows search results by
  `menu.removeAll()` + `menu.add(found.component())`. `Container.add` re-parents a component:
  a found nested entry is physically removed from its group.
- `Action.Restore` calls `RocketActionSettingsService.getAllExistsComponents()`, which is
  `rocketActionAndComponents.map { it.component }` — only the top-level list captured by the
  last full load. The current `swapContent` first calls `removeAll()` on the content menu.
- 0.25.0 removed the per-group `DomainEventSubscriber` that re-added children on
  `RestoreMenuDomainEvent`/`CreateMenuDomainEvent` (design D6 of that change). Its risk note
  assumed search does not touch group menus; search does.
- Constraints: `plugins/api` is a published binary contract; JDK 17; the reload budget
  (warm reload within 5 s, no growth across repetitions) and the "unchanged group is reused"
  guarantee both stay in force; all Swing re-attachment must be safe on the EDT.

## Goals / Non-Goals

**Goals:**

- One placement model describes the menu, and both the full load and the restore attach
  components according to it.
- A restore returns every search result to its group at any depth without re-creating
  actions, re-evaluating settings or calling plugins.
- A reload after a search produces the complete menu, so a manual Refresh is also a repair.
- Keep the O(actions) reload behaviour and the existing change-detection semantics.

**Non-Goals:**

- Changing the search semantics, the search index, or where search results are displayed.
- Making `RocketAction.component()` a factory (that would touch every plugin and the API).
- Rebuilding every group on every reload (rejected by 0.25.0 design D6 and by the reuse
  guarantee).
- Persisting any placement data; the snapshot lives in memory only.
- Caching the actions model across reloads.

## Decisions

### D1. A placement snapshot captured during the reload

`RocketActionSettingsService.loadAndGetAllComponents` already reads the whole
`ActionsModel`. After the model is read, it records a compact snapshot:

```
MenuStructure(
    roots: List<String>,                  // top-level ids, catalog order
    containers: Set<String>,              // ids whose component owns children (GROUP)
    children: Map<String, List<String>>,  // ordered child ids per container
)
```

It stores ids only — no `RocketActionSettingsModel` and no evaluated settings — so it does
not retain template data or models after the reload. The snapshot is `@Volatile` and replaced
on each successful model read.

Alternatives considered:

- *Retain the `ActionsModel` and walk it on restore.* Rejected: keeps the whole model and
  its settings in memory for the process lifetime for no benefit.
- *Rebuild the structure from the cached group menus.* Rejected: the cached menus are
  exactly what search damaged; the model is the only trustworthy source of order.
- *Store the parent/children in `RocketActionComponentCache`.* Rejected: the cache is a
  component store with a published mock in tests; placement is a menu concern, not cache
  state.

### D2. One assembly function used by load, restore and repair

Add a tree assembly that turns the snapshot plus the cache into attached components:

```
fun menuContent(): List<Component> {
    val structure = lastStructure ?: return emptyList()
    fun attach(id, path): Component? {
        if (id in path) { log cycle; return null }
        val component = cache.by(id)?.origin?.component() ?: return null
        if (id in structure.containers && component is JMenu) {
            component.removeAll()
            structure.children[id].orEmpty().forEach { attach(it, path + id)?.let(component::add) }
        }
        return component
    }
    return structure.roots.mapNotNull { attach(it, emptySet()) }
}
```

Every container is cleared and refilled in catalog order, so a component that search moved
to the content menu is pulled back when its group is processed. Because children are
attached recursively, any depth is repaired in one pass. `getAllExistsComponents()` becomes
this function (renamed), and `loadAndGetAllComponents` uses it for the final resolution
instead of the top-level-only loop.

Alternatives considered:

- *Restore only the entries of the last search view.* Rejected: requires extra "last view"
  state, and successive searches or a reload while the view is shown would still leave
  holes. The full pass is O(actions) and simpler.
- *Re-add every cached action to its group found by scanning all groups.* Rejected: same
  O(actions × groups) as the old event approach without a single source of order.

### D3. Apply the assembly on the EDT

The assembly only re-parents existing components; it does no plugin work and no evaluation.
The worker still builds components off the EDT (as today), but the final attach is applied in
`swapContent` on the EDT:

- `Create`: the worker returns the cache-filled result; `done()` applies the assembly to the
  content menu.
- `Restore`: the restore no longer needs a worker for the assembly. `resetSearch` clears the
  text and applies the assembly on the EDT. If the attach cost ever becomes visible on a very
  large catalog, it can stay in `SwingWorker` as long as the attach itself runs in `done()`.

`swapContent` remains the single place that empties and fills the content menu, so the
"previous content stays visible until the new one is ready" requirement holds.

Alternatives considered:

- *Keep restoring in the worker's `doInBackground`.* Rejected: the restore now mutates group
  menus of components that may be visible; all `removeAll`/`add` should be on the EDT.
- *Return a detached component tree from the worker.* Rejected: impossible while components
  are shared singletons.

### D4. Reuse is preserved

The assembly never calls `create`, `isChanged` or `RocketActionSettingsModel.to`; it only
calls `component()` on cached origins and `add`/`removeAll` on their containers. Therefore:

- the existing test `should recreate a group only when its child was rebuilt` keeps passing
  (the group instance and `create` count do not change);
- the reload counters (`created`, `evaluations`) do not grow;
- `failures` are not double counted: an id missing from the cache is skipped silently, as it
  was already counted when its creation failed.

### D5. Guards for cyclic and duplicate ids

The catalog is user-edited YAML; a group cycle (`A` contains `B`, `B` contains `A`) is
possible. `GroupRocketActionUi.create` currently degrades a cycle to a missing child; the
recursive assembly must not recurse forever, so it keeps a per-branch `path` set and skips an
id already on the path with a warning (once per rebuild, not per action).

Duplicate ids are already logged by `duplicatesOf`. Because the cache is keyed by id, two
placements of the same id share one component; the assembly places it at the first position
it is reached and skips later placements to avoid silently moving it. This is no worse than
today and is visible in the log.

### D6. Containers are recognized from the model

`containers` is the set of ids whose model type is `GROUP`. Only the group plugin consumes
cached children, and `fillCache` already special-cases that type. Restricting the
clear-and-refill to group ids avoids wiping menu items that another plugin's component may
create for itself.

## Risks / Trade-offs

- **The attach pass moves to the EDT and counts toward `swap`** → the pass is O(actions) of
  container operations only; on the reference catalog (2033 actions, 138 groups) it is
  expected to stay in the tens of milliseconds. The `swap` phase of the reload line shows it;
  if it ever dominates, the pass can be limited to a dirty subtree (tracked from the last
  search view) without changing the spec.
- **A search that shows a nested entry still detaches it while the search view is open** →
  accepted: the view is a temporary state. The next restore or reload rebuilds the full tree.
  If desired, the assembly can also run just before replacing the content menu with search
  results so consecutive searches never accumulate orphans; that is an optimization, not a
  correctness requirement.
- **Placement snapshot is stale after an external edit until the next reload** → intended:
  restore is not a refresh. A reload re-reads the file and replaces the snapshot, matching
  the existing freshness contract.
- **Duplicate ids remain ambiguous** → the cache model already collapses them; the assembly
  picks the first placement and logs the duplicates (existing warning). Changing this is out
  of scope.
- **A third-party plugin that returns a `JMenu` with its own children and declares nested
  settings with type `GROUP`** would have those children cleared → the type is the plugin's
  own contract; the same assumption exists in `fillCache`.

## Migration Plan

- No data or format migration; no configuration change.
- Build with the existing Gradle wrapper after the usual JDK 17 setup. Plugin jars do not
  need rebuilding: no `plugins/api` change.
- Rollback: redeploy the previous application jar; the change is confined to the application
  module.
- Verification: existing group/reload tests, the new restore tests, and a manual check on the
  reference catalog — search for an entry nested two groups deep, clear the search, refresh
  after a search, and confirm the menu, order and startup reload time are unchanged.
