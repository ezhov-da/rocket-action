package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType

data class InitValue(
    val value: String,
    val property: RocketActionConfigurationProperty?,
    val type: SettingsValueType?,
)


data class SpecValue(
    val value: String,
    val type: SettingsValueType?,
)
