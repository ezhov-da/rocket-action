# Spec Delta

## Purpose

Lets users choose the name under which an action is published to MCP clients, while keeping
generated names for every action that does not set one.

## ADDED Requirements

### Requirement: Optional custom MCP tool name per action

Atomic actions and chain actions SHALL carry an optional MCP tool name that the user can set.
The name is only meaningful for actions published as MCP tools.

#### Scenario: Custom name is stored with the action

- **WHEN** the user marks an atomic action or a chain action as used for MCP and enters a tool name
- **THEN** the entered name is persisted with that action

#### Scenario: Custom name is ignored for unpublished actions

- **WHEN** an action has a custom MCP tool name but is not marked as used for MCP
- **THEN** the action is not exposed as an MCP tool, and the custom name is not used

### Requirement: Tool name field is bound to the MCP checkbox

Each action dialog SHALL show an MCP tool name field next to the "Use for MCP" checkbox.
The field MUST be editable only while the checkbox is selected and is never required.

#### Scenario: Field is enabled by the checkbox

- **WHEN** the user selects the "Use for MCP" checkbox
- **THEN** the MCP tool name field becomes editable
- **AND** clearing the checkbox makes the field non-editable again

#### Scenario: Create dialog starts with an empty field

- **WHEN** the user opens the create dialog for an atomic action or a chain action
- **THEN** the MCP tool name field is empty and non-editable

#### Scenario: Editing shows the stored name

- **WHEN** the user edits an action that has a custom MCP tool name
- **THEN** the field shows that name along with the stored checkbox state

### Requirement: Empty custom name falls back to the generated name

When an action is published as an MCP tool, the system SHALL use the custom name if it is
non-blank after trimming and the generated name otherwise. The generated name keeps the
existing rule: transliterated action name, then `__`, then the action ID without dashes.

#### Scenario: Action without a custom name keeps its generated name

- **WHEN** a published action has no custom MCP tool name, or a name that is only whitespace
- **THEN** its MCP tool name is computed from the action name and action ID

#### Scenario: Custom name replaces the generated name

- **WHEN** a published action has a non-blank custom MCP tool name
- **THEN** its MCP tool is listed under exactly that name

### Requirement: Custom names are normalized

Custom MCP tool names SHALL be normalized before use: Cyrillic characters are transliterated,
characters outside `A-Z`, `a-z`, `0-9`, `_` and `-` are replaced with `_`, and the result is
limited to 128 characters. Whitespace around the entered value is ignored.

#### Scenario: Name with spaces and Cyrillic is normalized

- **WHEN** the user enters `Мой инструмент`
- **THEN** the published MCP tool name contains only Latin letters, digits, `_` or `-`

#### Scenario: Long custom name is truncated

- **WHEN** the user enters a custom name longer than 128 characters
- **THEN** the published MCP tool name is limited to 128 characters

### Requirement: Colliding tool names are rejected before saving

The action dialogs SHALL refuse to save a custom MCP tool name that normalizes to a name
already used by another action published as an MCP tool, and MUST report which action
conflicts. The action being edited is not a conflict with itself.

#### Scenario: Duplicate custom name is rejected

- **WHEN** the user saves an action whose custom MCP tool name equals the tool name of another published action
- **THEN** the changes are not saved and the dialog reports the conflict
- **AND** the user can correct the name and save again

#### Scenario: Unchanged name on the edited action is accepted

- **WHEN** the user saves an edited action without changing its already stored custom name
- **THEN** the save succeeds

### Requirement: MCP server keeps tool names unique at runtime

When two published actions resolve to the same tool name, the MCP server SHALL keep the first
tool and skip the duplicate, and MUST log a warning that identifies the skipped action.

#### Scenario: Duplicate names reach the server

- **WHEN** two published actions resolve to the same MCP tool name
- **THEN** only the first of them is registered as the MCP tool
- **AND** a warning is logged for the skipped action

### Requirement: Existing saved actions stay compatible

Saved action files written before this change SHALL keep loading, and their actions MUST keep
their previous MCP exposure and generated tool names.

#### Scenario: File without the custom name property is loaded

- **WHEN** an action file without a custom MCP tool name property is read
- **THEN** the action loads with no custom name and is published under its generated name
