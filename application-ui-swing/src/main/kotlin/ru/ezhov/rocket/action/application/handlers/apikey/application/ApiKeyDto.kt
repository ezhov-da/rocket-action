package ru.ezhov.rocket.action.application.handlers.apikey.application

data class ApiKeyDto(
    val value: String,
    val description: String? = null,
)
