package ru.ezhov.rocket.action.api.support

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPropertySpec

/**
 * Базовый класс для создания UI действия
 */
abstract class AbstractRocketAction : RocketActionFactoryUi, RocketActionConfiguration {
    protected fun createRocketActionProperty(
        key: String,
        name: String,
        description: String,
        required: Boolean,
        property: RocketActionPropertySpec = RocketActionPropertySpec.StringPropertySpec(),
    ): RocketActionConfigurationProperty =
        object : RocketActionConfigurationProperty {
            override fun key(): String = key
            override fun name(): String = name
            override fun description(): String = description
            override fun isRequired(): Boolean = required
            override fun property(): RocketActionPropertySpec = property
        }
}
