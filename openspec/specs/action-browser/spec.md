# action-browser Specification

## Purpose

Defines how the application lists, searches, resets, and renders AtomicAction and ChainAction lists — the Chains editor panels, the action-selection popup, and the chain editor action lists — so that browsing large action catalogs stays responsive and the displayed content is always current.

## Requirements

### Requirement: Search and reset do not block the UI

Submitting a search or applying a reset SHALL NOT block the UI event thread. Filtering, sorting, and preparing row decoration SHALL run outside the event thread, and the window SHALL accept input while a refresh is in progress.

#### Scenario: Search on a large list

- **WHEN** the user submits a search for a list that contains many actions
- **THEN** the window remains interactive and the filtered list replaces the previous content when the search completes

#### Scenario: Reset on a large list

- **WHEN** the user presses the reset control for a filtered list
- **THEN** the window remains interactive and the full list appears when the refresh completes

#### Scenario: Input while a refresh is running

- **WHEN** a list refresh is running and the user types in the search field
- **THEN** the typed characters are accepted without waiting for the refresh to finish

### Requirement: Repaint cost is bounded by visible rows

The time to lay out or repaint an action list SHALL depend on the rows currently visible and SHALL NOT grow with the total number of persisted actions, atomic action scripts, or scheduler records.

#### Scenario: Many persisted records

- **WHEN** a list is shown while many actions and scheduler records are persisted
- **THEN** refreshing and scrolling the list complete within a time that does not scale with the number of records that are not visible

#### Scenario: Repeated repaint

- **WHEN** the same list content is repainted repeatedly without any change
- **THEN** each repaint completes in the same bounded time and does not re-read persisted action or scheduler data

### Requirement: Reset restores the full list

The reset control of an action list SHALL clear the search text and restore the list of all items that match the active filter conditions, preserving the active sort order. This SHALL hold for the Chains editor panels and for the action-selection popup.

#### Scenario: Reset in the Chains editor

- **WHEN** a search text is present in the Chains or Actions editor panel and the user presses the reset control
- **THEN** the search field becomes empty and the panel lists all chains or atomic actions in the active sort order

#### Scenario: Reset in the action-selection popup

- **WHEN** a search text is present in the action-selection popup and the user presses the clear control
- **THEN** the popup list again shows all chains and atomic actions eligible for the popup

### Requirement: Only the latest search result is applied

When a new search or reset is submitted before the previous one completes, the earlier request SHALL be superseded. A stale result SHALL NOT replace the list content produced by a newer request.

#### Scenario: Superseded search

- **WHEN** a second search is submitted while the first search is still running
- **THEN** the list shows the result of the second search and the first result never overwrites it

#### Scenario: Reset supersedes a search

- **WHEN** the user resets the list while a search is still running
- **THEN** the list shows the complete list and the pending search result is discarded

### Requirement: Search and filter semantics are preserved

Search, filtering, and ordering of the action lists SHALL keep their current matching rules. AtomicAction text search SHALL match id, name, description, script data, and alias, including keyboard-layout transformation of the query. ChainAction text search SHALL match id, name, and description. Condition filters (contract type, engine, source, MCP usage, scheduler usage) SHALL combine with the text filter as before.

#### Scenario: AtomicAction text search

- **WHEN** the user searches for text that appears in an atomic action name, description, script body, or alias, or that matches after keyboard-layout transformation
- **THEN** that atomic action is included in the result

#### Scenario: ChainAction text search

- **WHEN** the user searches for text that appears in a chain id, name, or description
- **THEN** that chain is included in the result

#### Scenario: Condition filters

- **WHEN** the user selects a contract, engine, source, MCP, or scheduler condition and submits a search
- **THEN** the result contains only actions that satisfy both the text filter and the selected conditions, in the active sort order

### Requirement: Saved action data is visible immediately

After an AtomicAction, ChainAction, or scheduler change is saved through the application, the next list refresh SHALL reflect it without restarting the application. Cached data SHALL NOT cause the lists or their decorations to show outdated content.

#### Scenario: Edited action name

- **WHEN** an atomic action is saved with a new name
- **THEN** the next refresh of the Actions panel shows the new name

#### Scenario: Scheduler state change

- **WHEN** a scheduler is created, updated, or deleted for an action
- **THEN** the scheduler decoration of that action in the lists reflects the change after the next refresh

#### Scenario: Deleted action

- **WHEN** an atomic action or chain is deleted
- **THEN** it is absent from the next list refresh and no decoration for it remains
