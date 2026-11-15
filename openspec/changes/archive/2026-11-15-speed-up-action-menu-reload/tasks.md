# Tasks

## 1. Baseline and reload metrics

- [x] 1.1 Add an `ActionReloadScope` (application core) that collects phase marks and counters (`actions`, `groups`, `settingsEvaluated`, `cacheHits`, `failures`, `iconHits`, `iconMisses`) and can render one log line. Verify: a unit test asserts the rendered line contains every phase key and the counts it was given.
- [x] 1.2 Emit the line from `RocketActionSettingsService.loadAndGetAllComponents()` (settings read, cache fill, component resolution) and from `UiQuickActionService` after the EDT swap and after the MCP refresh completes, waiting at most two seconds for the refresh and falling back to `mcp=timeout`, while keeping the existing `Loading time and receiving all components '<n>' ms` line. Verify by running the app on the reference catalog: both lines appear once per reload, every phase key has a value, and the total matches the wall-clock time of the reload.
- [x] 1.3 Document the new metrics line in `README.md` (Logging) with one example, and note that a reload logs exactly one such line. Verify: the example in the README matches the real line from 1.2 character for character apart from the values.

## 2. Variables and password-store snapshot

- [x] 2.1 Add a single-flight, fingerprint-keyed cache to `VariablesApplication.all()` (fingerprint from `variables.json`, `configurations.json` and the configured `.kdbx`: path, mtime, size) and invalidate it from `save()`, `updateVariable()` and `ConfigurationApplication.save()`. Verify: a test with two temp variable files shows the second call does not re-read the repository (repository spy/mock counts one `all()` call).
- [x] 2.2 Wire explicit invalidation into the manual Refresh path (`UiQuickActionService.updateMenu`) so the user can force a re-read. Verify: a test (or the manual harness from 7.4) asserts the invalidation callback is invoked before the reload starts.
- [x] 2.3 Keep a failed password-store read non-fatal with a 30-second back-off and a single log entry, and return the variables without KeePass entries. Verify: `KeePassManagerRepository`/`VariablesApplication` test with a missing `.kdbx` returns the non-KeePass variables, logs once across repeated calls, and recovers after the back-off reset.
- [x] 2.4 Confirm no regression for the execution paths that already use `VariablesApplication` (`VariablesServiceImpl`, chain actions, `GroovyPluginLoader`) by running their existing tests. Verify: `.\gradlew.bat :application-ui-swing:test --tests "*VariablesFrame*" --tests "*ActionExecutorImpl*" --tests "*GroovyPluginLoader*"` passes.

## 3. One settings evaluation per action per reload

- [x] 3.1 Add `ActionReloadScope.settingsOf(actionId)` memoisation and `RocketActionSettingsModel.to(scope)`; make the returned view memoise its own `settings()` as well, and keep a compatibility `to(engineService)` overload that opens a short-lived scope. Verify: `YmlRocketActionSettingsRepositoryTest` and `TestPanel`-related tests still pass, and a new test calls `settings()` on the same view four times and observes one engine invocation.
- [x] 3.2 Use the scope in `RocketActionSettingsService.createAndPutToCache` (both `isChanged` and `create`) so the same action is evaluated once across all consumers. Verify: a test with a mocked `EngineService` and an action that is both checked and created asserts `processWithEngine` is called once per setting.
- [x] 3.3 Apply the same memoisation to `MutableRocketActionSettings` (configuration UI) so the editor's test panel does not re-evaluate on every access. Verify: its existing tests pass and a focused test counts one evaluation for two `settings()` reads.
- [x] 3.4 Log duplicate action ids once when the model is read. Verify: a test with a duplicated id produces one warning and the reload still completes.

## 4. Compiled Groovy template cache

- [x] 4.1 Add `GroovyScriptClassCache` (one shared `GroovyClassLoader`, `ConcurrentHashMap` keyed by template text, LRU bound 256) and make `GroovyEngine.execute` instantiate the compiled class with a fresh `Binding` instead of building a `GroovyShell`. Verify: `GroovyEngineTest` passes unchanged and a new test executes the same template twice and asserts the script class is identical (compile count one).
- [x] 4.2 Cover semantics that `evaluate` provided: variables visible as binding properties, the `variables` map binding, a template returning a value, and a template that mutates state between runs. Verify: new tests assert identical results to the previous behaviour, including a template executed concurrently from several threads.
- [x] 4.3 Verify the cache bound: exceed 256 distinct templates and assert eviction happens without an exception and a previously evicted template still evaluates correctly. Verify: the bound test passes.

## 5. Icons without blocking the reload

- [x] 5.1 Add an application-lifetime icon cache (URL → decoded 16×16 `Icon`, with a 10-minute `_failed_` marker) to `ResourceLoaderService`/`DiskCacheService`, and make `checkQuietly()` a one-time initialisation. Verify: `DiskCacheServiceTest`/`ResourceLoaderServiceTest` pass, and a test loads the same URL twice with a counting cache service and observes one disk/decode path.
- [x] 5.2 Make two-argument `load` non-blocking: return the default icon immediately when the icon is neither cached nor on disk, and fetch it in the background with connect/read timeouts. Verify: a test with a deliberately unreachable URL returns within milliseconds with the default icon and does not throw.
- [x] 5.3 Add the additive `IconService.load(iconUrl, defaultIcon, onLoaded)` overload in `plugins/api` with a default body delegating to the two-argument method, set the `plugin-api` version bump in `moduleVersions`, and switch the in-repo remote-icon call sites (`GroupRocketActionUi`, three `open-url` actions, `note`, `exec`) to the callback form that sets `menu.icon` on the EDT and repaints. Verify: `.\gradlew.bat clean build` succeeds, plugin fat jars are produced, and a test invokes the callback form with a stubbed loader and asserts the menu icon changes on the EDT.
- [x] 5.4 Document the callback overload and the non-blocking contract in the plugin documentation (`README.md` Plugins/API section and the `plugins/template` sample). Verify: the documented snippet compiles as written (or is the literal example used by a test).

## 6. Constant-time lookups

- [x] 6.1 Change `InMemoryRocketActionComponentCache.byIds` to iterate the requested ids and look them up in the map, preserving the result semantics. Verify: a new test returns only the requested entries (including an unknown id), and `actionsByIds` tests pass.
- [x] 6.2 Build a `Map<type, Success>` inside `PluginsReflectionRocketActionPluginRepository.load()` and make `by(type)` a map lookup, calling `configuration()` once per plugin. Verify: `PluginsReflectionRocketActionPluginRepositoryTest` passes and a test asserts `configuration()` is called once for two `by(type)` lookups.

## 7. Rebuild menu content without replacing the bar

- [x] 7.1 Split `UiQuickActionService.createMenu` into chrome creation (bar, content menu, tags menu, search field, chain button, tools menu, move handle) and content loading; keep the chrome instances in fields and never reassign `baseDialog.jMenuBar`. Verify: a test (or the manual harness in 7.4) triggers two reloads and asserts the bar, search field and tools menu instances are identical, and that typed text and focus survive.
- [x] 7.2 Stop clearing the content menu in `CreateMenuOrGetExistsWorker`'s constructor and stop replacing the menu icon while loading; apply `removeAll()` + `add(...)` in one EDT batch in `done()`, with a "Loading…" placeholder only when the menu is still empty. Verify: during a reload the previously displayed entries are still present and enabled, and the swap happens once (asserted by a counting container listener or a test around the extracted swap helper).
- [x] 7.3 Clear and refill the tags menu only when tags are enabled, and only from the content pass. Verify: with tags disabled no tags menu exists after several reloads; with tags enabled it is the same instance and reflects current tags.
- [x] 7.4 Update the Swing manual harness (`.run` configuration or an existing `main()` harness for the menu) to exercise: type text → Refresh → text survives; click Refresh twice → menu never empties. Verify: running the harness shows the described behaviour and the log of 1.2.

## 8. Group change propagation and failure isolation

- [x] 8.1 Add `ActionReloadScope.changed` and stop marking reused actions `NOT_CHANGED_SINCE_LAST_LOAD` before the group phase; normalise after all groups are built. Verify: a test builds a child, reloads with a changed child, and asserts the parent group's `create` is invoked again, while an unchanged reload reuses the group without recreating it.
- [x] 8.2 Make `GroupRocketActionUi.isChanged` consult the reload change set (settings, child set size, changed children) and remove the per-group `DomainEventSubscriber` registration. Verify: a test creates two groups and publishes `CreateMenuDomainEvent`/`RestoreMenuDomainEvent` repeatedly, asserting no subscribers accumulate (subscriber count stable) and no stale child component appears after a child is rebuilt.
- [x] 8.3 Make `GroupRocketActionUi.create` tolerate missing children: add resolvable children, log a warning with the group and missing ids, and count one failure; never throw. Verify: a test with a missing child gets a non-null group containing the available children.
- [x] 8.4 Wrap each cache-fill task and each component-resolution step in failure isolation, collect failures thread-safely, and show one aggregated notification on the EDT. Verify: a test where one plugin throws still returns the other components, and a notification test (or assertion on the collected failures) shows one aggregated message for two failures.
- [x] 8.5 Run the whole group's tests together with the existing cache/plugin tests. Verify: `.\gradlew.bat :application-ui-swing:test --tests "*Group*" --tests "*RocketActionComponentCache*" --tests "*RocketActionSettingsService*"` passes.

## 9. MCP refresh follows the swap

- [x] 9.1 Publish the content-replaced event (and keep `CreateMenuDomainEvent`) only after the content is applied on the EDT. Verify: a test subscriber records the event order relative to the content swap and asserts the swap comes first.
- [x] 9.2 Move `McpServerService.refreshTools()` onto a single-thread daemon executor subscribed to that event, and report its duration into the reload metrics. Verify: `McpToolNameTest`/`WebServerIntegrationTest` pass, and a test asserts the event handling returns before the refresh completes while the refresh is still executed exactly once.
- [x] 9.3 Verify tool list changes end to end: mark an action for MCP, reload, assert the tool appears; unmark, reload, assert it disappears. Verify: the scenario is covered by an integration test or recorded manual checks against a running instance.

## 10. Integration verification on the reference catalog

- [x] 10.1 Run the built application with the reference catalog (`D:\allinone\tools\rocket-action\.rocket-action\actions.yml`, 2033 actions) and record the warm reload from the metrics line. Verify: total stays within 5 s and `settings`/`cacheFill`/`components`/`swap` are each reported.
- [x] 10.2 Reload three times without editing anything and compare the phases. Verify: every repetition stays within the budget and no phase grows.
- [x] 10.3 Compare the evaluation counter against the catalog (67 engine-backed settings: 58 Groovy + 9 Mustache). Verify: the metrics line shows ~67 evaluations instead of the measured 270, and the variables phase shows one password-store read.
- [x] 10.4 Exercise the acceptance scenarios: menu usable and never blank during reload; search text and global hot key survive; icon host unreachable (blocked host) does not change reload time; a group pointing at a missing child still yields the rest of the menu with one notification; badge/tags menu behaves with tags enabled and disabled. Verify: each scenario is observed and noted in the change folder.
- [x] 10.5 Update `AGENTS.md` where the reload pipeline, the icon callback contract or the metrics line is referenced, so the next contributor does not reintroduce `settings()`-per-access or a per-reload KeePass read. Verify: the documented statements match the final code (grep for the old patterns returns only intentional occurrences).

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Re-check `openspec validate speed-up-action-menu-reload` before archiving.
