# action-menu-reload Specification

## Purpose

Defines how the application builds and rebuilds the action menu: what the user sees while a reload runs, how much work a reload is allowed to repeat, how failures and freshness are handled, and how the reload keeps in step with the actions published to MCP.

## Requirements

### Requirement: Reload keeps the current menu usable

While the action catalog is being reloaded, the application SHALL keep the previously built action menu visible and interactive, and SHALL replace its content only when the new content is complete.

#### Scenario: Reload with a menu already shown

- **WHEN** the user triggers a reload while a menu is already displayed
- **THEN** the previous actions remain visible and the menu stays responsive until the new content replaces them in a single step

#### Scenario: Reload of a large catalog

- **WHEN** a reload takes several seconds because the catalog is large
- **THEN** the window keeps repainting and the user can still open the existing menus

#### Scenario: Failed reload

- **WHEN** a reload fails before the new content is ready
- **THEN** the previously built menu remains available and the failure is reported to the user

### Requirement: Menu bar controls survive a reload

Controls that do not carry action content (the search field, the tools menu, the chain-action control and the drag handle) SHALL be preserved across a reload, including the text typed into the search field, focus, and configured global hot keys.

#### Scenario: Typed search text

- **WHEN** the user has typed text into the search field and a reload runs
- **THEN** the same text is still in the search field when the reload completes

#### Scenario: Global hot keys

- **WHEN** a global hot key that focuses the search field is configured
- **THEN** it still works after any number of reloads

### Requirement: Action settings are evaluated once per reload

Each action's effective settings SHALL be evaluated at most once per reload, and every consumer that needs them during that reload SHALL reuse the same result.

#### Scenario: Settings read repeatedly by a plugin

- **WHEN** an action's settings are requested several times while its plugin builds the menu entry
- **THEN** the template expressions of that action are evaluated once

#### Scenario: Evaluation count matches the catalog

- **WHEN** a catalog contains settings that use a template engine
- **THEN** a reload evaluates each of them once, independently of how many consumers read them

### Requirement: Variables and the password store are read at most once per reload

The system SHALL resolve user variables and the configured external password store at most once per reload, regardless of how many settings reference them.

#### Scenario: Many template settings with a password store configured

- **WHEN** a catalog contains many template settings and a password store is configured
- **THEN** the store is opened and decrypted once during the reload

#### Scenario: Variables saved during the session

- **WHEN** variables are saved through the application
- **THEN** the next evaluation uses the new values

#### Scenario: Variables configuration saved

- **WHEN** the variables key or the password store settings are saved
- **THEN** the next reload uses the new configuration

### Requirement: Remote icons do not block the menu

Building a menu entry SHALL NOT wait for a remote icon: the entry SHALL be created immediately with its default icon, and a remote icon SHALL replace it when it becomes available. Icons that are already available locally SHALL be used immediately.

#### Scenario: Slow or unreachable icon host

- **WHEN** an action references an icon URL whose host is slow or unreachable
- **THEN** the menu entry appears with the default icon and the reload finishes without waiting for the network

#### Scenario: Icon already available locally

- **WHEN** the icon is present in the local cache
- **THEN** the entry shows it as soon as it is added to the menu

#### Scenario: Shared icon source

- **WHEN** several actions reference the same icon source
- **THEN** that source is resolved once per application run and reused

### Requirement: One failing action does not prevent the rest

A failure while creating a menu entry or while reading one action's settings SHALL be isolated: the remaining actions SHALL still appear, the failure SHALL be logged, and the user SHALL be notified.

#### Scenario: Group references a missing child

- **WHEN** a group references a child action that is not present in the catalog
- **THEN** the other actions and groups appear and the failure is reported

#### Scenario: A plugin throws while creating an entry

- **WHEN** a plugin throws an exception while creating its menu entry
- **THEN** that entry is skipped and the rest of the menu appears

### Requirement: Reload reflects saved and externally edited data

A reload SHALL reflect the current persisted catalog, including changes saved through the configuration UI and changes made to the actions file outside the application.

#### Scenario: Action renamed through the configuration UI

- **WHEN** an action is renamed, saved through the configuration UI, and a reload follows
- **THEN** the menu shows the new label

#### Scenario: Actions file edited on disk

- **WHEN** the actions file is modified outside the application and a reload follows
- **THEN** the menu reflects the change

#### Scenario: Changed settings are not reused

- **WHEN** an action's settings change between two reloads
- **THEN** nothing computed from the previous values is reused for that action

### Requirement: MCP tool list follows the reload

After a reload, the actions published to MCP SHALL be exposed as MCP tools with their current names, and the menu SHALL become usable without waiting for the tool list to be updated.

#### Scenario: Action published to MCP

- **WHEN** an action is marked for MCP and a reload follows
- **THEN** its tool appears in the MCP tool list

#### Scenario: Action unpublished from MCP

- **WHEN** the MCP mark is removed and a reload follows
- **THEN** the tool disappears from the MCP tool list

#### Scenario: Update ordering

- **WHEN** a reload runs
- **THEN** the new menu content is applied before the MCP tool list is refreshed

### Requirement: Reload reports per-phase timings

Every reload SHALL log, at info level, the duration of each of its phases (settings read, cache fill, component resolution, menu swap, MCP refresh) together with the number of actions processed.

#### Scenario: Measurement

- **WHEN** a reload completes
- **THEN** the log contains one line with every phase duration and the action count

#### Scenario: Diagnosis of a slow reload

- **WHEN** a reload is slower than expected
- **THEN** the log identifies which phase dominates

### Requirement: Reload performance budget

On a warm start with the reference catalog of about two thousand actions, a reload SHALL complete within five seconds, and a repeated reload with unchanged actions SHALL NOT take longer than the previous one.

#### Scenario: Warm reload

- **WHEN** the reference catalog is reloaded with warm caches
- **THEN** the reported total reload time stays within the budget

#### Scenario: Repeated reload

- **WHEN** the reload is repeated without any change
- **THEN** every repetition stays within the budget and no phase grows from one reload to the next
