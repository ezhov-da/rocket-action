# Design

## Context

Tool names are currently derived on every MCP server build in `McpServerService.toolName()`:
`translit(action.name) + "__" + action.id.replace("-", "")`, sanitized and truncated to 128
characters. Nothing about the name is persisted, and `McpAction` carries only the action plus
its contract type. Actions are stored as JSON through `AtomicActionDto`/`ChainActionDto`, with
defaults used to read files written by older versions. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**

- One implementation of name resolution shared by the MCP server and the action dialogs, so
  the dialog check and the published name can never disagree.
- Zero migration: saved actions without the new property keep their behavior.
- Keep MCP naming out of the domain model beyond a plain data accessor.

**Non-Goals:**

- Renaming tools of already saved actions, or migrating generated names to explicit ones.
- Guaranteeing that renames of actions or new normalization rules keep old tool names stable.
- Bulk editing tool names from the actions/chains lists.

## Decisions

**1. Store the custom name on `AtomicAction` and `ChainAction`, and expose it through `Action`.**

`Action` gains `fun mcpToolName(): String? = null`; both models override it with a new
`var mcpToolName: String? = null` property. The interface default keeps the domain model free
of MCP logic and avoids touching any other `Action` consumer; `duplicate()` copies the value.
Alternative rejected: a separate name registry file — more state to keep consistent and no
benefit for a single optional value.

**2. Persist the raw trimmed text; normalize on use.**

DTOs get `var mcpToolName: String? = null` (no `WRITE_NULL` requirement, Jackson default
handling already used by the repositories). The value is stored as typed, only trimmed.
Normalization runs at resolution time, so improving the rules later does not rewrite user
data. Alternative rejected: normalizing on save — it mangles Cyrillic input in JSON and makes
the field's meaning depend on when it was saved.

**3. Add a single `McpToolName` helper and a resolved name on `McpAction`.**

New object `McpToolName` in `application.handlers.server.mcp` owns `translit`, `normalize`,
`generated`, `resolve(custom, id, name)`, `conflict(candidate, actions, exceptId)` and
`published(actions)`. The
existing transliteration map moves there from `McpServerService`. `McpAction` gains a computed
`toolName` using `resolve`. Both dialogs call the same helper, so validation matches
publishing. Alternative rejected: keeping the logic in `McpServerService` — the Swing dialogs
would have to duplicate transliteration and sanitization.

**4. Empty or blank custom names fall back to the generated name.**

`resolve` uses the custom value when it is non-blank after trimming, otherwise
`generated(id, name)`. This preserves the current names for every existing action.

**5. Reject collisions in the dialog, and guard at runtime.**

On save, a dialog resolves the candidate name and asks `McpToolName.conflict` over published
atomic actions and chains, excluding the edited action's own ID. A conflict shows a
`JOptionPane` message naming the conflicting action and aborts the save. The atomic dialog
therefore also needs `ChainActionService`; `ActionsConfigurationPanel` already receives it, so
only the dialog constructor changes. At runtime `McpServerService` logs a warning and keeps the
first action when two published actions resolve to the same name, so a hand-edited JSON file
cannot break the MCP tool list. That guard lives in `McpToolName.published`, so it can be
unit-tested without an MCP server. Both checks compare resolved names, which also catches a
custom name that collides with somebody else's generated name.

**6. UI: one row, field state driven by the checkbox.**

`JCheckBox` and `JTextField` share a row in both dialogs; the field starts disabled and is
toggled by an `addActionListener` on the checkbox, and is cleared when create dialogs reset.
The field carries a tooltip explaining the fallback and normalization, because the entered
value is not what is published (`Мой инструмент` becomes `Moy_instrument`). No live preview is
added.

## Risks / Trade-offs

- **[Stored raw values drift from published names]** → the tooltip states that invalid
  characters are replaced, and the field is optional, so most users keep the generated name.
- **[Cross-dialog dependency]** → `CreateAndEditAtomicActionDialog` gains `ChainActionService`,
  which grows its constructor and the test fixtures; contained to the UI package.
- **[Duplicate names can still be created by editing JSON outside the app]** → runtime guard
  keeps the first tool and logs a warning instead of failing the server build.
- **[Tool names of existing users can change if they later set a custom name]** → expected and
  explicit; no automatic migration exists to preserve them.

## Migration Plan

No data migration. New JSON property is optional and defaults to `null`; a rollback to a
previous build simply ignores it (the DTOs already use `@JsonIgnoreProperties`).
