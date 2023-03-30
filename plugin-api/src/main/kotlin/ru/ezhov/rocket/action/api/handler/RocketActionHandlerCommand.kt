package ru.ezhov.rocket.action.api.handler

data class RocketActionHandlerCommand(
    val commandName: String,
    val arguments: Map<String, String>
)
