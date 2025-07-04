package ru.ezhov.rocket.action.application.applicationConfiguration.domain.model

data class ApplicationConfigurations(
    var variablesKey: String,
    var numberButtonsOnChainActionSelectionPanel: Int,
    var globalHotKeys: GlobalHotKeys?
)

data class GlobalHotKeys(
    val activateSearchField: String?,
    val activateChainActionField: String?,
)
