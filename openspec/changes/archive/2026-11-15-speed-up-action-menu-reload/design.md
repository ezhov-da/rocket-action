# Design

## Context

See `proposal.md` — Why for the measurements and the problem statement. The facts that shape the approach:

- A reload is `UiQuickActionService.createMenu()` → `CreateMenuOrGetExistsWorker` → `RocketActionSettingsService.loadAndGetAllComponents()`. That method reads the whole actions file, fills the component cache (non-group actions in a 20-thread pool, groups sequentially in reverse order), then resolves the top-level components. `createMenu()` runs on the EDT and the resulting `JMenuBar` replaces the current one; the worker's constructor calls `baseMenu.removeAll()`, so the menu is empty for the whole load.
- `RocketActionSettingsModel.to(engineService)` returns a fresh anonymous `RocketActionSettings` on every call. Its `settings()` evaluates every setting through `EngineService`, which asks `VariablesApplication.all()` for the variable list **per setting**. Nothing is memoised at any level: model, view, service, or persistence.
- `VariablesApplication.all()` loads `variables.json`, `configurations.json` twice, and — when a KeePass manager is configured — opens and decrypts the whole `.kdbx` through `KeePassManagerRepository`. Both repositories re-read their files on every call.
- `GroovyEngine.execute` builds `new GroovyShell(...)` and evaluates the template text, i.e. compiles it, for every value. The reference catalog has 58 Groovy template settings that produce 270 evaluations per reload.
- `ResourceLoaderService.load` always goes through `DiskCacheService`, which runs `checkQuietly()` (TLS `SSLContext` + `SecureRandom`) on every call, downloads synchronously without timeouts, and never caches the decoded `Icon` or a failed download.
- `GroupRocketActionUi.create` throws when a child is missing, and registers a new `DomainEventSubscriber` for every group instance it creates; subscribers are never removed and `InMemoryDomainEventPublisher.unsubscribe` exists but is unused by the group.
- `RocketActionSettingsService.loadAndGetAllComponents` marks reused actions `NOT_CHANGED_SINCE_LAST_LOAD` before the group phase, so `GroupRocketActionUi.isChanged` (which inspects the cache state) can no longer see that a child was rebuilt, and its event subscriber is the only thing that repairs children at menu-add time.
- Constraints: Swing EDT must stay free; `plugins/api` is a published binary contract; JDK 17; no new dependencies are needed for any of the measures below; the actions file, `configurations.json`, `variables.json` and the `.kdbx` remain untouched formats.

## Goals / Non-Goals

**Goals:**

- One evaluation of an action's settings per reload, one variable/password-store read per reload, one compile per distinct Groovy template per process run.
- Never wait for the network while building menu entries, and never replace the menu bar to change its content.
- Keep the reload pipeline single-threaded where it must be (groups) and parallel where it pays off, without thread pools that leak.
- Failures stay local: one broken action or plugin does not cost the user the whole menu.
- Make the reload measurable from the log alone.

**Non-Goals:**

- Changing what actions appear, their order, their labels, or how plugins behave when the user runs them.
- Caching the actions model across reloads, or watching the file for changes. A reload still reads the file once; freshness stays explicit.
- Proactively prefetching remote icons at startup, or persisting a decoded-icon cache across runs (the existing disk cache of downloaded bytes stays).
- Making the configuration UI, chain editor, search popup or MCP protocol faster beyond the shared wins (variables snapshot, `byIds`).
- Coalescing or debouncing reload requests; a queued single-flight guard is added, but it is not the mechanism that makes the reload fast.
- Virtualising or lazily populating the action menu.

## Decisions

### D1. A per-reload evaluation scope owns repeated work

Introduce `ActionReloadScope` (application core) created by `RocketActionSettingsService.loadAndGetAllComponents()` and closed in a `finally`. It exposes:

- `settingsOf(actionId): Map<String, String>` — a `ConcurrentHashMap` memo so every consumer that asks for the same action during this reload gets one evaluation. `RocketActionSettingsModel.to(scope)` returns a view whose `settings()` reads from the scope (and also memoises locally, so a plugin calling `settings()` four times, as `GroupRocketActionUi.create` does, costs nothing extra).
- `markChanged(id)` / `isChanged(id)` — the set of actions actually rebuilt during this reload (see D6).
- counters for the metrics line (settings evaluated, cache hits, failures).
- a handle to the variables snapshot of D2 so evaluation does not depend on a service-lookup chain.

`RocketActionSettingsModel.to(engineService)` keeps a compatibility overload that opens a short-lived scope, so the configuration UI's `TestPanel` and the old-format reader keep compiling; the reload path always passes a scope.

Alternatives considered:

- *Memoise only inside the anonymous `RocketActionSettings` view.* Rejected: `create` and `isChanged` obtain two different views for the same action, so the two evaluations per action remain.
- *Memoise in the `RocketActionSettingsModel` instance.* Rejected: the fields are `var` (the configuration UI mutates them), so a cache there needs its own invalidation and would silently serve stale values after an edit while the object is still referenced.
- *Global static cache by action id.* Rejected: breaks the "changed settings are not reused" scenario and leaks between reloads.

Settings evaluation stays on the worker threads (inside `create`), so it keeps the parallelism of the non-group phase; the scope only removes duplicates. Because a scope is created per reload, the "settings once per reload" requirement holds even if a plugin keeps its view object.

### D2. One variables snapshot per reload, fingerprinted and invalidated

`VariablesApplication.all()` becomes a memoised, single-flight read:

- The memo holds `(fingerprint, VariablesDto)`. The fingerprint is built from `variables.json` (path, mtime, size), `configurations.json` (path, mtime, size) and the configured KeePass `dbPath` (path, mtime, size). Three `stat` calls per reload replace thousands of `JacksonDatabase.load` calls and per-property AES-GCM decryptions. Using stat instead of only in-process invalidation also covers the case where the user edits the files outside the application, which the existing behaviour supports.
- Concurrent callers share one `CompletableFuture`; the first caller computes, the others wait. This is what removes the `Cipher`/`SecureRandom` contention: exactly one thread decrypts.
- `save()`, `updateVariable()` and `ConfigurationApplication.save()` invalidate the memo explicitly. `UiQuickActionService`'s manual Refresh also invalidates it before starting a reload, so the user always has an escape hatch even if a fingerprint is missed.
- Environment and system properties are captured with the snapshot instead of being re-listed per call.
- A failing password store keeps today's behaviour: log once and return the variables without KeePass entries. The failure is remembered for a short back-off (30 s) so a corrupt file does not make every setting attempt to open it again.

`VariablesApplication` is a Spring singleton used by execution paths too (`VariablesServiceImpl`, chain actions, Groovy plugin loader), so this also removes repeated loads outside the reload. The fingerprint is re-checked per call, so external edits are picked up without the application noticing they happened.

Alternatives considered:

- *Scope-only snapshot (no global memo).* Rejected: plugins and the execution paths outside the reload would keep re-reading; the memo costs one fingerprint check.
- *TTL cache.* Rejected as the primary mechanism: a TTL either serves stale variables for a while after a save or expires in the middle of a reload. Explicit invalidation plus a fingerprint is deterministic.
- *Invalidate only from save paths.* Rejected: misses external edits, which the current implementation honours.

### D3. Compile each Groovy template once

Add `GroovyScriptClassCache` next to `GroovyEngine`:

- Key: the template text. Value: the compiled `Class<*>` obtained from one shared `GroovyClassLoader` (parent = the application class loader) via `parseClass`.
- Execution: instantiate the class per call, attach a fresh `Binding` with the variables, and `run()`. This is semantically what `GroovyShell.evaluate` does internally, minus the compile.
- Bounded LRU (256 entries) so a pathological configuration cannot grow metaspace without limit; classes already loaded stay loaded, and eviction only costs one recompilation if the template comes back.
- `computeIfAbsent` keeps it lock-free in the common case; a racing duplicate compile produces an identical class and is harmless.

Alternatives considered:

- *One `GroovyShell` per thread.* Rejected: `evaluate(String)` recompiles every time; only the shell (and its class loader) would be reused.
- *Precompile all templates when the actions model is read.* Rejected for now: it moves the cost to the reload's critical path and needs cross-reload invalidation. Lazy compilation is paid once per distinct template and then disappears from the budget.

The Mustache engine is left alone: 9 settings in the reference catalog, not a measured hotspot.

### D4. Icons: memory cache, no network on the caller, late replacement

`ResourceLoaderService` gains an application-lifetime icon cache and never touches the network on the calling thread:

- Key: the icon URL. Value: the decoded, scaled `Icon` or a `_failed_` marker with a 10-minute expiry.
- Caller path: memory hit → return. Otherwise, if the disk cache already has the file, decode it inline (this is the "icon already available locally" case) and memoise. Otherwise return the default icon at once and submit the download to a small bounded executor; on completion it decodes, memoises, and, when a callback was supplied, delivers the icon.
- The plugin API (`plugins/api`) gets an additive overload `load(iconUrl, defaultIcon, onLoaded: (Icon) -> Unit)` with a default body delegating to the existing two-argument `load`, so existing plugin binaries keep working. In-repo call sites that load remote icons (`GroupRocketActionUi`, the three `open-url` dialogs, `note`, `exec`) pass a callback that runs on the EDT, sets `menu.icon` and repaints the entry. `plugin-api` version is bumped and the plugin fat jars are rebuilt, per the project convention.
- Two-argument `load` keeps working for third-party plugins but is now *never blocking*: a cold URL yields the default icon and warms the cache, so the next reload shows it. This is the graceful-degradation contract; the spec's "replaces when available" scenario is met by the callback form used by the in-repo plugins.
- `checkQuietly()` becomes a one-time initialisation (guarded static flag), not a per-icon `SSLContext`/`SecureRandom` allocation. The background fetch sets connect/read timeouts (~5 s/10 s), because the current `url.openStream()` has none and an unreachable host used to hold a reload thread for a minute.
- The download executor and the reload executor are different pools: icon downloads must not consume the threads that build actions, and they stay daemon threads.

Alternatives considered:

- *A deferred `Icon` implementation that swaps its delegate.* Rejected: `Icon` has no reference to the component that paints it, so the change would not be repainted without extra bookkeeping; the callback gives the plugin the component it already holds.
- *Blocking decode of everything on a background prefetch at startup.* Rejected as out of scope; correctness (no blocking) matters more than the first paint of a remote icon.
- *Clearing the icon cache on every reload.* Rejected: it would re-decode hundreds of icons for no benefit; an edited `iconUrl` is a new key anyway.

### D5. Rebuild the menu content; keep the menu bar

Split `UiQuickActionService.createMenu` into two responsibilities:

- **Chrome, built once per dialog:** the `JMenuBar`, the content `JMenu`, the tags menu (when `IS_ENABLE_TAGS`), the search field, the "available chain actions" button, the tools menu, and the move handle. These instances are kept in fields and are never recreated by a reload, so typed search text, focus, hot-key registration and the configured chain button survive.
- **Content, rebuilt per reload:** only the children of the content `JMenu`. `updateMenu()` starts a worker that computes the component list off the EDT and, in `done()`, does one `removeAll()` + `add(...)` batch on the EDT followed by `revalidate()`/`repaint()`. The bar is not replaced and `baseDialog.jMenuBar` is not reassigned.
- `CreateMenuOrGetExistsWorker` no longer calls `removeAll()` in its constructor and no longer sets the loader icon on the content menu; the previous entries stay enabled and clickable. Progress is shown by disabling the "Refresh" item and adding a small "Loading…" item only when the menu has no content yet (first start), removed on the swap.
- The tags menu is cleared and refilled from the same content pass, and only when tags are enabled; today a fresh `JMenu` is built on every load.
- Search and restore keep operating on the same content menu, so `fillMenuByRocketAction` needs no change beyond being aware that it is mutating persistent chrome state and that the next reload restores the full list.

Alternatives considered:

- *Keep replacing the bar but snapshot/restore the chrome.* Rejected: re-registering hot keys, restoring caret position and focus, and re-adding the drag handle is fragile and would race with the swap.
- *Card layout with two content menus.* Rejected as more machinery than the problem needs; `removeAll` + `add` on one menu is atomic enough for Swing repaint purposes.

### D6. Change propagation for groups without per-group subscribers

Replace the currently broken state path with an explicit per-reload change set:

- During the load, an action that is actually rebuilt is added to `ActionReloadScope.changed`; reused actions are **not** marked `NOT_CHANGED_SINCE_LAST_LOAD` before the group phase (that is what hides child changes today). The normalisation to `NOT_CHANGED` happens after all groups are built, so group change detection sees the truth.
- `GroupRocketActionUi.create` is invoked only when its settings changed or any of its direct children is in the changed set or its child count changed. Its `isChanged` reads the scope instead of the cache state, keeping the same signature and satisfying `RocketAction`'s contract.
- The per-group `DomainEventSubscriber` (which currently leaks one subscriber per group per reload and makes every event cost O(reloads × groups)) is removed. Group contents are built from the cache when the group is created, and an unchanged group keeps its (still valid) child components because reused actions return the same `origin`. The group no longer needs to be repaired at menu-add time.
- Missing children no longer throw: `create` adds the children it can resolve, logs a warning naming the group and the missing ids, and reports one failure to the caller. `ActionReloadScope` counts it.

Alternatives considered:

- *Keep subscribers and unsubscribe when the group is replaced.* Rejected: a group can be replaced while a queued event still references it, and the group has no lifecycle hook to hang disposal on.
- *Rebuild every group on every reload.* Rejected: it discards valid components and re-adds hundreds of menu items on the EDT for no user-visible difference.

### D7. Failure isolation at the service boundary, not inside each plugin

- `createAndPutToCache` wraps each task in `try/catch (Throwable)`: log with action id/type, count it, keep going. The cached entry for a failed action is left absent, and the action's parent group simply skips it.
- Component resolution in `loadAndGetAllComponents` is wrapped per action for the same reason.
- `RocketActionSettingsModel.settings()` keeps returning an empty map on engine failure, so one failing template does not poison the action's other settings.
- Failures are collected thread-safely and surfaced once, on the EDT, as one notification ("N actions could not be built; see the log"), instead of one notification per failure from arbitrary pool threads.
- The reload itself never throws out of the worker: `done()` catches `ExecutionException`, logs the aggregated cause and leaves the previous content in place.

Rationale: plugins are third-party code and the reference catalog already contains a group with a missing child. The service boundary is the only place that can guarantee "the rest of the menu appears" without auditing every plugin.

### D8. Constant-time lookups

- `InMemoryRocketActionComponentCache.byIds` iterates the requested ids and looks each one up, instead of streaming and filtering the whole map.
- `PluginsReflectionRocketActionPluginRepository` builds a `Map<type, Success>` inside `load()` (the only place plugins are (re)loaded) and `by(type)` becomes a map read. `configuration(context)` is evaluated once per plugin instead of once per action per lookup. Plugins are loaded exactly once per process today, so no invalidation is needed; the guard that prevents a second `load()` stays.

### D9. MCP refresh after the swap, off the reload path

- The reload publishes a single `MenuContentReplacedDomainEvent` in `done()`, after the content is visible on the EDT. `CreateMenuDomainEvent` keeps being published for backward compatibility at the same point.
- `McpServerService` subscribes to both and performs `refreshTools()` on a single-thread daemon executor, so tool rebuilds are serialised and cannot delay the menu. Its duration is reported as the `mcp` phase of the metrics line.
- `McpServerService` failures are logged and never propagate into the Swing worker.

### D10. One metrics line per reload

`ActionReloadScope` collects `System.nanoTime()` marks and counters and logs at INFO:

```
Menu reload: total=…ms actions=… groups=… settings=… evaluations=… variables=…ms cacheFill=…ms components=…ms swap=…ms mcp=…ms iconHits=… iconMisses=… failures=…
```

`settings` is the actions-file read (spec phase "settings read"), `cacheFill` and `components` are the fill and resolution phases, `swap` is measured on the EDT, `mcp` is filled in when the MCP refresh finishes. The existing `Loading time and receiving all components '…' ms` line is kept for one release (it is what the measurements and the user's habit refer to) and emitted from the same scope. The single line is emitted once both the swap and the MCP refresh have completed, so every phase has a real value; the logger waits for the async refresh on a background thread for at most two seconds, and falls back to `mcp=timeout` (with the duration logged separately) if the tool rebuild is slower than that. The menu is already visible before any of this, so the wait only affects the log line, never the UI.

### D11. Thread model

- Keep the two-phase cache fill (parallel non-groups, sequential groups in reverse order) because groups must be created after their children and the group menu construction touches Swing components.
- Replace the per-reload `Executors.newFixedThreadPool(20)` with a shared bounded daemon executor owned by the application (`min(20, max(4, availableProcessors × 2))` workers). Pool creation and teardown per reload is pure overhead, and a shared pool makes the "no phase grows from one reload to the next" scenario easier to reason about.
- `loadAndGetAllComponents` waits for the parallel phase with a single `Future` over the submitted tasks rather than `awaitTermination` on a pool it just shut down, so a failure in one task cannot silently truncate the wait.
- The manual Refresh path is single-flight: if a reload is running, at most one follow-up is queued. This is a guard against double work when the user clicks Refresh repeatedly, not the performance mechanism.

## Risks / Trade-offs

- **Stale variables if a fingerprint misses** (file content changed with identical size and mtime) → the fingerprint includes path, size and nanosecond mtime; manual Refresh forces invalidation; the reload also invalidates when it is triggered by a save.
- **KeePass load failure is cached for 30 s** → the menu still builds without KeePass variables; the log says so once; a manual Refresh after fixing the file may need to wait out the back-off. Mitigation: the back-off only applies to failures, and it is reset by explicit invalidation.
- **Icon cache never expires** → a remote icon whose bytes change under the same URL stays stale until restart. Accepted: `iconUrl` changes produce a new key, and the previous behaviour had no memory cache at all but the same disk-cache staleness.
- **First cold icon is only a default icon** for call sites that do not use the callback (third-party plugins) → in-repo plugins use the callback; the API default keeps old plugins working, and they get the icon on the next reload.
- **Removing group event subscribers changes restore-after-search behaviour** → group menus are not touched by search, and unchanged groups keep valid child components; this must be verified manually (task) because `RestoreMenuDomainEvent` also guarded against a stale child count. The scope's change set makes the count check unnecessary.
- **Compiled Groovy classes are cached for the process lifetime** (LRU bounds the map, not the class loader) → templates come from user configuration and are few; a pathological config with thousands of distinct templates would grow metaspace. Mitigation: bounded map plus a log warning when the bound is hit.
- **`settings()` memo keyed by action id assumes unique ids** → duplicates would collapse to one evaluation (and the component cache already keys by id, so the behaviour is no worse). The loader logs duplicate ids when reading the model.
- **`removeAll()` + `add()` of a large top-level menu on the EDT** can still cause a visible pause with very large catalogs → it is now the only EDT work besides the swap, is measured as the `swap` phase, and the previous content stays enabled until it happens. Chunking is out of scope.
- **Not blocking on icons changes perceived behaviour** (`icon` appears later) → visible only on a cold cache; the disk cache means most users see icons immediately.
- **API change in `plugins/api`** requires rebuilding plugin jars and bumping `plugin-api` version in `moduleVersions` → done in the same change; third-party jars stay loadable because the old method is unchanged and the new one has a default body.

## Migration Plan

- No data migration: `actions.yml`, `configurations.json`, `variables.json`, `.kdbx` and `.rocket-action/cache` keep their formats.
- Build order: `plugins/api` (version bump) → plugins → application. `copy-app-to-prod.sh`/`copy-all-to-prod.sh` as usual.
- Rollback: redeploy the previous application jar; the API addition is additive, so old plugin jars keep working with the new API and new plugin jars require the new API (as with any version bump).
- Verification on the reference catalog: the new metrics line must show one variables load, ~67 settings evaluations (58 Groovy + 9 Mustache) instead of 270, and a warm total within the budget; the menu must stay visible during the reload; a deliberately broken group must leave the rest of the menu intact; an unreachable icon host must not change the reload time.

## Open Questions

- Whether the legacy `Loading time and receiving all components` log line is dropped after one release, or kept as the permanent total. Deferrable: both are emitted from the same metrics object, so removing one later is a one-line change.
