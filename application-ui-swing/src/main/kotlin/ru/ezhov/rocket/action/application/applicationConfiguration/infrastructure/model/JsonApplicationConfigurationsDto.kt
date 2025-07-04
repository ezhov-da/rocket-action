package ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.GlobalHotKeys

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonApplicationConfigurationsDto(
    var variablesKey: String?,
    var numberButtonsOnChainActionSelectionPanel: Int?,
    var globalHotKeys: JsonGlobalHotKeys?
)

data class JsonGlobalHotKeys(
    val activateSearchField: String?,
    val activateChainActionField: String?,
)
