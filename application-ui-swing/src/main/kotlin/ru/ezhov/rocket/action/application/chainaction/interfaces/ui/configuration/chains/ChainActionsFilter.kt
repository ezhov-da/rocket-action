package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains

import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction

/**
 * Search, sort and condition rules of the Chains editor list. Extracted so the behavior can be
 * covered by regression tests without starting the Swing UI.
 */
object ChainActionsFilter {
    fun filter(
        sortInfo: SortInfo,
        searchAction: SearchAction,
        chains: List<ChainAction>,
        scheduledActionIds: Set<String> = emptySet(),
    ): List<ChainAction> {
        val sortedChains = when (sortInfo.sortField) {
            SortField.NAME -> when (sortInfo.direction) {
                Direction.ASC -> chains.sortedBy { it.name }
                Direction.DESC -> chains.sortedByDescending { it.name }
            }

            SortField.ACTIONS_COUNT -> when (sortInfo.direction) {
                Direction.ASC -> chains.sortedBy { it.actions.size }
                Direction.DESC -> chains.sortedByDescending { it.actions.size }
            }
        }

        val filterByText = when (searchAction) {
            is SearchAction.SearchInfo -> {
                if (searchAction.text.isEmpty()) {
                    sortedChains
                } else {
                    sortedChains
                        .filter { ch ->
                            searchAction.text.any { st ->
                                ch.id.lowercase().contains(st) ||
                                    ch.name.lowercase().contains(st) ||
                                    ch.description.lowercase().contains(st)
                            }
                        }
                }
            }

            is SearchAction.Reset -> sortedChains
        }

        var filterByConditions = filterByText
        searchAction.conditions.forEach { condition ->
            when (condition) {
                SearchAction.SearchCondition.MCP -> filterByConditions =
                    filterByConditions.filter { it.isTool }

                SearchAction.SearchCondition.SCHEDULER -> filterByConditions =
                    filterByConditions.filter { it.id in scheduledActionIds }

                else -> {}
            }
        }

        return filterByConditions
    }
}
