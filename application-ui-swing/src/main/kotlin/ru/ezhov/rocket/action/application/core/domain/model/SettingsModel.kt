package ru.ezhov.rocket.action.application.core.domain.model

data class SettingsModel(
    val name: String,
    val value: String,
    val valueType: SettingsValueType? = null
)
