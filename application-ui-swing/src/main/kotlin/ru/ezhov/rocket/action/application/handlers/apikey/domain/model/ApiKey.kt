package ru.ezhov.rocket.action.application.handlers.apikey.domain.model

data class ApiKey(
    val value: String,
    val description: String? = null,
)
