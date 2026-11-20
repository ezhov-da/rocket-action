# Spec Delta

## ADDED Requirements

### Requirement: Clearing the search restores the full menu structure

Clearing the menu search SHALL rebuild the complete action menu from the current catalog: every entry that the search showed SHALL reappear inside its original group, at its original position and at any nesting depth.

#### Scenario: Entry nested in a group

- **WHEN** the user searches and the search view shows an entry that belongs to a group, and the user then clears the search
- **THEN** that entry is again inside its group and the group contains all of its entries in catalog order

#### Scenario: Entry nested several groups deep

- **WHEN** a found entry is nested more than one group deep and the user clears the search
- **THEN** each intermediate group again contains all of its entries in catalog order

#### Scenario: Entry at the top level

- **WHEN** the search shows entries that are direct children of the root menu and the user clears the search
- **THEN** the root menu shows every top-level entry in catalog order

#### Scenario: Reset by keyboard

- **WHEN** the search view is shown and the user presses Esc, or submits an empty search
- **THEN** the menu is restored exactly as after clearing the search with the clear control

#### Scenario: Repeated searches

- **WHEN** the user runs several searches in a row and clears the search after the last one
- **THEN** no entry is missing from any group

### Requirement: A reload after a search produces the complete menu

A reload that follows a search SHALL produce the same menu structure and order as a reload performed without any preceding search.

#### Scenario: Manual refresh after a search

- **WHEN** the user refreshes the menu after a search, without clearing the search first
- **THEN** the menu contains every catalog entry inside its group, in catalog order

#### Scenario: No entry lost by the search

- **WHEN** the same catalog is reloaded before and after a search
- **THEN** both reloads produce the same set of entries with the same grouping and order

#### Scenario: Search through the tags menu

- **WHEN** the user selects a tag that shows an entry nested in a group and then clears the menu search
- **THEN** the entry returns to its group like an entry found through the search field
