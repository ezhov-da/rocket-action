package ru.ezhov.rocket.action.api

import ru.ezhov.rocket.action.api.context.RocketActionContext

/**
 * Action Plugin
 */
interface RocketActionPlugin {
    /**
     * UI Factory
     */
    fun factory(context: RocketActionContext): RocketActionFactoryUi

    /**
     * Action Configuration
     */
    fun configuration(context: RocketActionContext): RocketActionConfiguration

    /**
     * Info
     */
    fun info(): RocketActionPluginInfo
}
