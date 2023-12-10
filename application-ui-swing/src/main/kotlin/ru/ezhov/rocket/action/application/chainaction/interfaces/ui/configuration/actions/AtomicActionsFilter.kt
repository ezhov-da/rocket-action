package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType

object AtomicActionsFilter {
    fun filter(sortInfo: SortInfo, searchAction: SearchAction, actions: List<AtomicAction>): List<AtomicAction> {
        val atomics = actions

        val sortedAtomics = when (sortInfo.sortField) {
            SortField.NAME -> when (sortInfo.direction) {
                Direction.ASC -> atomics.sortedBy { it.name }
                Direction.DESC -> atomics.sortedByDescending { it.name }
            }

            SortField.ENGINE -> when (sortInfo.direction) {
                Direction.ASC -> atomics.sortedBy { it.engine }
                Direction.DESC -> atomics.sortedByDescending { it.engine }
            }

            SortField.CONTRACT -> when (sortInfo.direction) {
                Direction.ASC -> atomics.sortedBy { it.contractType }
                Direction.DESC -> atomics.sortedByDescending { it.contractType }
            }

            SortField.SOURCE -> when (sortInfo.direction) {
                Direction.ASC -> atomics.sortedBy { it.source }
                Direction.DESC -> atomics.sortedByDescending { it.source }
            }
        }

        val filterByText = when (searchAction) {
            is SearchAction.SearchInfo -> {
                if (searchAction.text.isEmpty()) {
                    sortedAtomics
                } else {
                    sortedAtomics
                        .filter {
                            it.name.lowercase().contains(searchAction.text.lowercase()) ||
                                it.description.lowercase().contains(searchAction.text.lowercase()) ||
                                it.data.lowercase().contains(searchAction.text.lowercase()) ||
                                it.alias?.lowercase()?.contains(searchAction.text.lowercase()) ?: false
                        }
                }
            }

            is SearchAction.Reset -> sortedAtomics
        }

        var filterByConditions = filterByText
        searchAction.conditions.forEach { condition ->
            when (condition) {
                SearchAction.SearchCondition.IN_OUT -> filterByConditions =
                    filterByConditions.filter { it.contractType == ContractType.IN_OUT }

                SearchAction.SearchCondition.IN_UNIT -> filterByConditions =
                    filterByConditions.filter { it.contractType == ContractType.IN_UNIT }

                SearchAction.SearchCondition.UNIT_OUT -> filterByConditions =
                    filterByConditions.filter { it.contractType == ContractType.UNIT_OUT }

                SearchAction.SearchCondition.UNIT_UNIT -> filterByConditions =
                    filterByConditions.filter { it.contractType == ContractType.UNIT_UNIT }

                SearchAction.SearchCondition.KOTLIN -> filterByConditions =
                    filterByConditions.filter { it.engine == AtomicActionEngine.KOTLIN }

                SearchAction.SearchCondition.GROOVY -> filterByConditions =
                    filterByConditions.filter { it.engine == AtomicActionEngine.GROOVY }

                SearchAction.SearchCondition.TEXT -> filterByConditions =
                    filterByConditions.filter { it.source == AtomicActionSource.TEXT }

                SearchAction.SearchCondition.FILE -> filterByConditions =
                    filterByConditions.filter { it.source == AtomicActionSource.FILE }
            }
        }

        return filterByConditions
    }
}
