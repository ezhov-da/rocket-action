package ru.ezhov.rocket.action.api.support

import ru.ezhov.rocket.action.api.PropertyType
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi

/**
 * Базовый класс для создания UI действия
 */
abstract class AbstractRocketAction : RocketActionFactoryUi, RocketActionConfiguration {
    protected fun createRocketActionProperty(
        key: RocketActionConfigurationPropertyKey,
        name: String,
        description: String,
        required: Boolean,
        type: PropertyType = PropertyType.STRING,
        default: String? = null
    ): RocketActionConfigurationProperty =
        object : RocketActionConfigurationProperty {
            override fun key(): RocketActionConfigurationPropertyKey = key
            override fun name(): String = name
            override fun description(): String = description
            override fun isRequired(): Boolean = required
            override fun type(): PropertyType = type
            override fun default(): String? = default
        }
}