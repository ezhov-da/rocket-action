package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi

/**
 * Базовый класс для создания UI действия
 */
abstract class AbstractRocketAction : RocketActionFactoryUi, RocketActionConfiguration {
    protected fun createRocketActionProperty(
            key: String,
            name: String,
            description: String,
            required: Boolean
    ): RocketActionConfigurationProperty =
            object : RocketActionConfigurationProperty {
                override fun key(): String = key

                override fun name(): String = name

                override fun description(): String = description

                override val isRequired: Boolean = required
            }
}