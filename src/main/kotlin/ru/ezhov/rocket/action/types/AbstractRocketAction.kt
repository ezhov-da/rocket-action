package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionUi

/**
 * Base class for UI action
 */
abstract class AbstractRocketAction : RocketActionUi, RocketActionConfiguration {
    protected fun createRocketActionProperty(
            name: String,
            description: String,
            required: Boolean
    ): RocketActionConfigurationProperty {
        return object : RocketActionConfigurationProperty {
            override fun name(): String = name

            override fun description(): String = description

            override val isRequired: Boolean = required
        }
    }
}