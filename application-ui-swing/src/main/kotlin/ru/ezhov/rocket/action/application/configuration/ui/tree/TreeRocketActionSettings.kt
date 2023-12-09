package ru.ezhov.rocket.action.application.configuration.ui.tree

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.application.core.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi

private val logger = KotlinLogging.logger {}

data class TreeRocketActionSettings(
    val configuration: RocketActionConfiguration,
    val settings: MutableRocketActionSettings,
) {

    // TODO ezhov maybe it’s worth switching to editing and keeping descendants up to date
    fun asString(childCount: Int? = 0): String =
        configuration.asStringDynamic(settings.settings.associate { it.name to it.value })
            ?: (
                configuration
                    .asString()
                    .firstNotNullOfOrNull { k ->
                        val v = settings.settings.firstOrNull { it.name == k }?.value
                        if (v != null && !v.isNullOrEmpty()) {
                            if (settings.type == GroupRocketActionUi.TYPE && childCount != null) {
                                "$v ($childCount)"
                            } else {
                                v
                            }
                        } else {
                            null
                        }
                    }
                    ?: run {
                        val value = settings.type

                        logger.debug {
                            "The property specified for display was not found '${configuration.asString()}' " +
                                "for type ${configuration.type()}. Set $value"
                        }

                        if (value == GroupRocketActionUi.TYPE && childCount != null) {
                            "$value ($childCount)"
                        } else {
                            value
                        }
                    }
                )
}
