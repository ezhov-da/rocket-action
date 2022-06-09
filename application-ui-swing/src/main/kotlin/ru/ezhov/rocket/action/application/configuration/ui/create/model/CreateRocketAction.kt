package ru.ezhov.rocket.action.application.configuration.ui.create.model

data class CreateRocketAction(
    val type: String,
    val properties: Map<String, String>
)
