# Proposal

## Why

Actions published as MCP tools get their tool name generated from the action name and ID
(`translit(name) + "__" + id`). Users cannot choose a shorter, stable or more descriptive
name for the AI client, and renaming an action silently renames its MCP tool.

## What Changes

- Atomic actions and chain actions gain an optional `mcpToolName` value.
- The "Use for MCP" row in both action dialogs gains an "MCP tool name" text field that is
  enabled only while the checkbox is selected and may stay empty.
- A non-blank custom name is normalized (translit, invalid characters replaced with `_`,
  trimmed to 128 chars) and used as the MCP tool name; an empty value keeps the current
  generated name, so existing actions keep working unchanged.
- Colliding tool names are rejected in the dialog (message naming the conflicting actions)
  and, as a runtime guard, the MCP server keeps the first tool and logs a warning.
- Saved JSON stays backward compatible: missing `mcpToolName` reads as `null`.

## Capabilities

### New Capabilities

- `mcp-tools`: how actions are published as MCP tools, including tool-name resolution,
  normalization, uniqueness and backward compatibility.

### Modified Capabilities

- None.

## Impact

- Domain: `AtomicAction`, `ChainAction`; DTOs `AtomicActionDto`, `ChainActionDto`;
  JSON repositories `JsonAtomicActionRepository`, `JsonChainActionRepository`.
- MCP: `McpAction` / `McpActionRocketActionService` (name passed with the action) and
  `McpServerService` (resolution, normalization, duplicate guard).
- UI: `CreateAndEditAtomicActionDialog`, `CreateAndEditChainActionDialog`.
- Saved state in `.rocket-action/atomic-actions.json` and `.rocket-action/chain-actions.json`
  gains an optional property.
