package ru.ezhov.rocket.action.application.core.infrastructure.yml.model

data class RocketActionSettingsDto(
    val id: String,
    val type: String,
    val settings: List<SettingsDto>,
    val actions: List<RocketActionSettingsDto>,
    val tags: List<String> = emptyList(),
)
