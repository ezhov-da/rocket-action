package ru.ezhov.rocket.action.application.domain.model

data class SettingsModel(
    val name: String,
    val value: String,
    val valueType: SettingsValueType? = null
)
