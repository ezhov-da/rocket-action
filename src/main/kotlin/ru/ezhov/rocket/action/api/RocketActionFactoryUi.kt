package ru.ezhov.rocket.action.api

/**
 * UI action builder
 */
interface RocketActionFactoryUi {
    /**
     * Component creation should only happen when this method is called.
     *
     * @return component to display
     */
    fun create(settings: RocketActionSettings): RocketAction?

    /**
     * @return action type
     */
    fun type(): String
}