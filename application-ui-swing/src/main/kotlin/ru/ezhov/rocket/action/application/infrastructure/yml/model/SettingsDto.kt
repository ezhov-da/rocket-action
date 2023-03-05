package ru.ezhov.rocket.action.application.infrastructure.yml.model

data class SettingsDto(
    val name: String,
    val value: String,
    val valueType: SettingsValueTypeDto? = null
)
