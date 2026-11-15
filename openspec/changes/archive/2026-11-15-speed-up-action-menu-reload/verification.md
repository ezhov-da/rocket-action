# Verification notes

## Automated checks (run locally)

| Task | Command | Result |
|------|---------|--------|
| 2.4 | `.\gradlew.bat :application-ui-swing:test --tests "*VariablesFrame*" --tests "*ActionExecutorImpl*" --tests "*GroovyPluginLoader*"` | passed |
| 3.1 | `... --tests "*YmlRocketActionSettingsRepository*" --tests "*TestPanel*"` | passed |
| 8.5 | `... --tests "*Group*" --tests "*RocketActionComponentCache*" --tests "*RocketActionSettingsService*"` | passed |
| 9.2 | `... --tests "*Mcp*" --tests "*WebServerIntegrationTest"` | passed |
| 9.3 | `... --tests "*McpToolListRefreshTest*"` | passed (new test, see below) |
| 10.2 | `... --tests "*RocketActionSettingsServiceReloadTest*"` | passed (new test, see below) |
| 7.4 / 10.4 | `... --tests "*UiQuickActionServiceReloadTest*"` | passed (extended test, see below) |
| 5.3 | `.\gradlew.bat clean build` | passed; all `plugins/*/build/libs/*-fat.jar` produced |
| all | `.\gradlew.bat :application-ui-swing:test` | 183 tests, 0 failed, 3 skipped |

## Tests added for the remaining items

### 9.3 — MCP tool list follows mark/unmark

`McpToolListRefreshTest` runs a **real** `McpSyncServer` (the tool registry is the one from
the MCP SDK) with only the action storage replaced by an in-memory repository. Flow:

1. nothing is marked → `listTools()` is empty;
2. mark the action for MCP → `refreshTools()` → the tool `Tool_Action__toolid` appears;
3. unmark → `refreshTools()` → the tool is removed.

No `.rocket-action` file is read or written and no network is used.

### 10.2 — three consecutive reloads

`RocketActionSettingsServiceReloadTest.should keep the phases and the work stable across
three consecutive reloads` builds a synthetic catalog (1 group with 2 template children +
3 template leaves) and reloads it three times with a fresh `ActionReloadScope` each time:

- the first reload creates 5 actions, the next two create 0 (everything is reused);
- the engine call count is identical on all three reloads (settings are evaluated once per
  action per reload, not once per consumer);
- `actions=6`, `groups=1`, `failures=0`, `evaluations` identical on all three;
- every phase (`settings`, `cacheFill`, `components`, `swap`, `variables`, `total`) is
  within the 5 s budget and none is `-1`;
- `render()` contains every `METRIC_KEYS` entry on every reload.

Timing phases on the synthetic catalog are sub-millisecond and noisy, so the "no phase
grows" criterion is verified structurally (no re-creation, no re-evaluation) plus the
per-reload budget check; the reference-catalog phases are recorded below.

### 7.4 / 10.4 — menu stays usable across refreshes

The manual Swing harness was replaced by the automated one
(`UiQuickActionServiceReloadTest`, which drives a real `JDialog` + `JMenuBar` on the EDT):

- `should keep the typed text and a non empty menu across three consecutive refreshes` —
  types text, clicks Refresh, types again, clicks Refresh again; the content menu is
  non-empty during every reload, the last content is displayed and the typed text and the
  search field instance survive;
- `should keep the chrome and swap the content only when it is ready` — the bar, the search
  field and the tools menu are the same instances; the previous entries stay visible for the
  whole reload;
- `should keep one tags menu instance when tags are enabled` — the tags menu is not
  recreated.

## Reference catalog (2033 actions)

The application was started on a **copy** of `.rocket-action/actions.yml` in an isolated
temporary working directory (startup actions disabled, HTTP port 4599, plugin folder read
from the installation). No file of the production installation was written. The KeePass
database was absent in the copy, so KeePass variables were skipped after the expected
non-fatal `FileNotFoundException` and 30-second back-off.

Observed on the initial reload:

```
Loading time and receiving all components '1994' ms
Menu reload: total=2643ms actions=2033 groups=138 settings=55ms evaluations=66 variables=0ms cacheFill=1931ms components=3ms swap=0ms mcp=10ms iconHits=33 iconMisses=72 failures=2
```

- `settings`, `cacheFill`, `components`, `swap` are all reported; `total` is within the 5 s
  budget (10.1).
- `evaluations=66` matches the expected ~67 template-backed settings (58 Groovy + 9
  Mustache) instead of the previous 270 engine executions; the password store was read once
  and then skipped (30 s back-off) — `variables=0ms` (10.3).
- `failures=2` are two groups referencing missing children; the groups were still built with
  their available children (the missing-child tolerance from 8.3) and the aggregated
  notification path was used.
- Both log lines appear exactly once per reload.

## 10.4 scenario coverage

| Scenario | Evidence |
|----------|----------|
| menu usable and never blank during reload | `UiQuickActionServiceReloadTest` (content stays populated on every reload) |
| search text and focus survive | `UiQuickActionServiceReloadTest` (same search field instance, text preserved) |
| global hot key survives | the hot key is registered in `createSearchField()`, which runs only when the chrome is created; the test asserts the search field instance is unchanged across reloads |
| unreachable icon host does not change reload time | `ResourceLoaderService` tests: two-argument `load` returns within milliseconds without network |
| group pointing at a missing child yields the rest with one notification | `GroupRocketActionUiTest` + `RocketActionSettingsServiceReloadTest` (aggregated notification) |
| badge/tags menu with tags enabled and disabled | `UiQuickActionServiceReloadTest` (tags on: same instance; tags off: no tags menu) |

## Scope of this verification

All items above were verified by the automated tests listed and by the recorded run on an
isolated copy of the reference catalog. A human interactive session on a real desktop (visual
flicker, actual global hot key press) was **not** performed in this environment: 7.4 and 10.4
are closed by their automated equivalents, not by manual observation.

## Bugs found by the new tests and fixed

- The main menu lost its icon: the old `CreateMenuOrGetExistsWorker` set `JMenu.icon` to
  `AppIcon.LOADER` while loading and to `AppIcon.ROCKET_APP` when the content was ready, and
  the chrome/content split dropped both assignments. The icon is now set once when the chrome
  is built (`AppIcon.ROCKET_APP`) and is never replaced during a reload; the reload test
  asserts the instance.
- `JMenu.componentCount` is always `0` (a `JMenu` stores items in its popup menu). The
  reload used it to decide whether to add the "Loading…" placeholder and therefore appended
  a placeholder on every reload; `itemCount` is the correct API.
- `ChainActionUi` initialised its service references only in `configuration()`. After
  `PluginsReflectionRocketActionPluginRepository` started resolving the type once per
  plugin, `create()` could run with the initialised fields reset to `null`. The
  initialisation is now idempotent and also runs from `factory()`.
