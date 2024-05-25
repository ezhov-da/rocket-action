package ru.ezhov.rocket.action.application.configuration.ui.edit

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType

data class Value(
    val key: String,
    val value: String,
    val property: RocketActionConfigurationProperty?,
    val valueType: SettingsValueType?,
)
