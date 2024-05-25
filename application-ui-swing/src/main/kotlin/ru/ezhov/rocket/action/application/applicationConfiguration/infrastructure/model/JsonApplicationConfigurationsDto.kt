package ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonApplicationConfigurationsDto(
    var variablesKey: String?,
    var numberButtonsOnChainActionSelectionPanel: Int?,
)
