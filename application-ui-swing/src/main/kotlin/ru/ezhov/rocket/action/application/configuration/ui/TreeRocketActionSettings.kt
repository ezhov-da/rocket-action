package ru.ezhov.rocket.action.application.configuration.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.application.infrastructure.RocketActionSettingsNode

private val logger = KotlinLogging.logger {}

data class TreeRocketActionSettings(
    val configuration: RocketActionConfiguration,
    val node: RocketActionSettingsNode,
) {
    fun asString(): String =
        node.to().let { settings ->
            configuration
                .asString()
                .firstNotNullOfOrNull { k ->
                    val v = settings.settings()[k]
                    if (v == null || v.isEmpty()) {
                        null
                    } else {
                        v
                    }
                }
                ?: run {
                    val value = settings.type().value()

                    logger.debug {
                        "The property specified for display was not found '${configuration.asString()}' " +
                            "for type ${configuration.type()}. Set $value"
                    }

                    value
                }
        }
}
