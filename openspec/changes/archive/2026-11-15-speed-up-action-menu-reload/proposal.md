# Proposal

## Why

Rebuilding the action menu — on startup and on the manual "Refresh" that follows a configuration edit — is slow and looks frozen. With the reference catalog (45 top-level actions, 2033 actions in total, 138 groups) the reload takes 52.5 s on a cold start and 12.8 s when everything is already warm, and the action menu is empty for the whole time because `createMenu` replaces the menu bar and the worker calls `baseMenu.removeAll()` before loading.

Profiling (10 thread dumps over the parallel fill phase, plus the debug log) shows the time is not spent on plugins: loading 19 plugin jars and instantiating all 22 plugin classes takes 178 ms. The time goes to recomputing the same values over and over:

- 35% of sampled fill-cache threads sit inside a full KeePass database load (`JacksonDatabase.load` + per-property AES-GCM decryption), because `VariablesApplication.all()` is invoked once per engine-processed setting and nothing is cached. Per-property `Cipher`/`SecureRandom` creation also serialises the pool on a JVM-wide lock (50 of 242 samples were `BLOCKED`).
- 270 Groovy template evaluations take 27 s of thread time (avg 100 ms, max 1602 ms) because `GroovyEngine.execute` builds a `new GroovyShell` and recompiles the script for every value.
- `RocketActionSettingsModel.settings()` has no memoisation, so every access re-runs the engines; plugins call it once per setting key, `create` and `isChanged` call it again, and 67 template settings turn into 270 evaluations.
- Icons are re-decoded on every access (no `Icon` cache, `SSLContext` + `SecureRandom` re-created per `DiskCacheService.get`), plugin lookup is a linear scan calling `configuration()` per plugin, and the group cache lookup scans the whole cache.

A single broken action also aborts the whole reload: one group whose child could not be created throws `Action by '<id>' is not found` and the menu stays empty.

## What Changes

- Compute the effective settings of an action once per model instead of on every `settings()` access, and reuse the compiled Groovy template instead of recompiling it per value.
- Resolve variables (and therefore the KeePass database) at most once per reload, with explicit invalidation when variables, configuration or the keystore change.
- Memoise icons by source and initialise the HTTPS socket factory once instead of per icon.
- Make plugin-by-type and cache-by-id lookups constant-time instead of linear scans.
- Rebuild the *content* of the action menu instead of replacing the whole menu bar; keep the previous content visible until the new content is ready, so the menu never goes empty.
- Build the tags menu only when tags are enabled, and refresh the MCP tool list without holding up the menu swap.
- Isolate failures per action: one action or plugin that fails to build must not prevent the rest of the menu from appearing.
- Log per-phase timings (settings read, cache fill, component resolution, menu swap, MCP refresh) on every reload.

## Capabilities

### New Capabilities

- `action-menu-reload`: how the action menu is built and rebuilt — responsiveness of the UI during a reload, preserving the visible content until the new content is ready, the work-reuse guarantees (settings, variables, icons), freshness after a save or an external edit, failure isolation, and the MCP tool list staying in sync.

### Modified Capabilities

None. `mcp-tools` keeps its current requirements (tool naming, uniqueness, compatibility); only the moment the tool list is refreshed moves inside the reload described by the new capability. `action-browser` covers the Chains editor lists, not the main menu.

## Impact

- `RocketActionSettingsService` (cache fill, component resolution, metrics), `RocketActionSettingsModel` (settings memoisation), `EngineService`/`GroovyEngine` (variable and template reuse).
- `VariablesApplication`, `KeePassVariableRepository`, `KeePassManagerRepository`, `JsonFileVariableRepository` (caching and invalidation), plus the save paths that must invalidate it.
- `UiQuickActionService` (menu bar reuse, no blanking, tags menu, MCP event ordering), `McpServerService` (subscribe to the new ordering).
- `GroupRocketActionUi` and other plugins that call `settings()` repeatedly; `PluginsReflectionRocketActionPluginRepository`, `InMemoryRocketActionComponentCache`.
- `ResourceLoaderService`/`DiskCacheService` (icon memoisation, one-time TLS setup).
- No public API, HTTP/MCP contract, or persisted file format changes. Behaviour stays compatible: the same actions appear in the same order, and MCP tools keep their names.
