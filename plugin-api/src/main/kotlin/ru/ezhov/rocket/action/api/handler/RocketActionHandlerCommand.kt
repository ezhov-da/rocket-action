package ru.ezhov.rocket.action.api.handler

import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey

data class RocketActionHandlerCommand(
    val commandName: String,
    val arguments: Map<RocketActionConfigurationPropertyKey, String>
)
