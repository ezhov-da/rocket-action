package ru.ezhov.rocket.action.application.configuration

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.application.infrastructure.RocketActionSettingsNode
import ru.ezhov.rocket.action.core.domain.model.ActionSettingName

private val logger = KotlinLogging.logger {}

data class NewTreeRocketActionSettings(
    val configuration: RocketActionConfiguration,
    val settings: RocketActionSettingsNode,
) {
    fun asString(): String =
        configuration
            .asString()
            .firstNotNullOfOrNull { k ->
                val v = settings.settings.map[ActionSettingName(k.value)]
                if (v == null || v.value.isEmpty()) {
                    null
                } else {
                    v.toString()
                }
            }
            ?: run {
                val value = settings.to().type().value()

                logger.debug {
                    "The property specified for display was not found '${configuration.asString()}' " +
                        "for type ${configuration.type()}. Set $value"
                }

                value
            }
}
