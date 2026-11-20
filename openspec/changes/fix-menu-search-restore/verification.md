# Verification

Change: `fix-menu-search-restore`.

## Automated tests

`.\gradlew.bat :application-ui-swing:test` — green (JDK 17). New cases:

| Test | Covers |
|------|--------|
| `MenuStructureTest.should keep ids and catalog order of nested groups` | 1.1 — the snapshot keeps ids and order of one-, two- and three-level groups |
| `MenuStructureTest.should keep ids only and not retain the model` | 1.1 — the snapshot fields are `List<String>`/`Set<String>`/`Map<String, List<String>>`; no model is retained |
| `RocketActionSettingsServiceRestoreTest.should clear and refill groups at any nesting depth in catalog order` | 1.2 — assembly attaches one-, two- and three-level nesting in catalog order |
| `RocketActionSettingsServiceRestoreTest.should return a found nested entry to its group on restore` | 1.4 — a search re-parents a nested entry, the restore returns it to the group with the full child count |
| `RocketActionSettingsServiceRestoreTest.should place a duplicated id only once` | 1.5 — a duplicate is placed once, no entry is lost |
| `RocketActionSettingsServiceRestoreTest.should skip a cyclic reference with one warning per rebuild` | 1.5 — a cycle does not overflow the stack and warns once per reload |
| `RocketActionSettingsServiceRestoreTest.should produce the complete menu after a reload that follows a search` | 1.6 / 2.3 — a reload after a search repairs the tree and does not rebuild anything |
| `UiQuickActionServiceSearchResetTest.should restore the menu on Esc and reset the search field` | 2.1 — Esc restores on the EDT, the text is cleared and the background is reset |
| `UiQuickActionServiceSearchResetTest.should restore the menu on Ctrl+Enter with an empty field` | 2.1 — the empty Ctrl+Enter branch uses the same restore |
| `UiQuickActionServiceSearchResetTest.should restore the menu from the clear button` | 2.1 — the Clear button uses the same restore |
| `UiQuickActionServiceSearchResetTest.should return a tagged nested entry to its group after reset` | 2.2 — a tag search moves a nested entry out, the reset returns it |
| `UiQuickActionServiceSearchResetTest.should keep every entry after two searches and one reset` | 2.4 — two searches in a row, one reset, every entry present |

Existing suites that must stay green (and do):

- `RocketActionSettingsServiceReloadTest` — `should recreate a group only when its child was
  rebuilt`, the `created`/`evaluations` counters and the 5 s reload budget (1.3);
- `UiQuickActionServiceReloadTest` — the chrome, the typed text and the single EDN swap batch
  survive a reload (2.3);
- `GroupRocketActionUiTest` — no per-group subscriber is installed (the 0.25.0 guarantee).

## Reference catalog

Catalog: `D:\allinone\tools\rocket-action\.rocket-action\actions.yml` (not a repository file).

### Structure and the assembly on the real catalog

A temporary check (removed afterwards, procedure below) loaded the catalog with the real
`YmlRocketActionSettingsRepository`, built the snapshot and ran the real
`RocketActionSettingsService` with the real `GroupRocketActionUi` for groups and a fake plugin
for every other type (a unique `JMenuItem` per id). It then compared every group's children with
the snapshot, moved a leaf nested two groups deep into a separate `JMenu` to emulate the search
view, and restored.

```
REFERENCE CATALOG: roots=45 actions=2035 groups=138 duplicates=0 cold=727ms warm=[109, 108, 103]
```

- 45 top-level entries, 2035 actions, 138 groups, no duplicate ids — the whole tree (every group,
  every depth) matches the snapshot in order after the reload **and** after the simulated search
  plus restore; the search view is empty afterwards and the found entry is back in its group.
- `cold` includes creating the fake components; `warm` is the assembly alone, repeated three
  times: ~100 ms for 2035 components, i.e. the restore/reload re-attachment stays in the
  `components` phase and does not threaten the 5 s warm budget.

Reproduce: create a JUnit test that
1. reads the catalog through `YmlRocketActionSettingsRepository(catalog.toUri(), engineMock)`,
2. builds the service with `RocketActionComponentCacheFactory.cache`, the real
   `GroupRocketActionUi` for `GROUP` and a fake plugin returning one `JMenuItem` per id,
3. loads once, then `restoreMenuContent()` and compares each `JMenu`'s items with
   `MenuStructure.of(model.actions).children[id]`,
4. moves a leaf of a group nested two levels deep into a new `JMenu`, calls
   `restoreMenuContent()` and repeats the comparison.

### Application reload line

The application (built `rocket-action.jar` of this working tree) was started on an **isolated
copy** of the catalog in a temporary working directory (plugin folder read from the repository
`plugin-jars`, HTTP port 4599, startup actions disabled by the empty copy). No file of the
production installation was written. KeePass was absent in the copy, so the password store was
skipped as before.

```
Loading time and receiving all components '2051' ms
Menu reload: total=2117ms actions=2035 groups=138 settings=61ms evaluations=67 variables=0ms cacheFill=1978ms components=4ms swap=2ms mcp=0ms iconHits=0 iconMisses=63 failures=10
```

- The line appears once, every phase key has a value, and `total` is within the 5 s budget.
- `components=4ms` is the new recursive assembly of 2035 components over 138 groups; it is the
  same O(actions) pass the previous top-level-only resolution performed, so the reload cost is
  unchanged (the bulk is still `cacheFill`).
- `actions=2035` / `groups=138` match the structure check above. `failures=10` are groups
  referencing children that were not built (some plugin types are absent from the local
  `plugin-jars`); this is the existing missing-child tolerance (`GroupRocketActionUi` logs the
  ids and shows the available children). The new assembly does not add failures: an id that is
  not in the cache is skipped without counting it again, and `component()` errors are counted
  once per reload.
- A warm (second) reload could not be triggered without an interactive session: the only
  in-process triggers are the Refresh menu item and the configuration editor's
  `RefreshUiEvent`. Warm behaviour is covered by
  `RocketActionSettingsServiceReloadTest.should keep the phases and the work stable across
  three consecutive reloads` (three reloads in the same process, each within the 5 s budget,
  `created=0` after the first) and by the repeated assembly measurements above.

### Scope of this verification

No human interactive session (visual click-through of the real menu) was performed in this
environment. The search/clear/refresh scenarios are covered by the automated Swing tests listed
above, which drive real `JMenu`/`JMenuItem` trees on the EDT, and by the reference-catalog check
that loads the real catalog and verifies every group's contents after a simulated search and a
restore.
