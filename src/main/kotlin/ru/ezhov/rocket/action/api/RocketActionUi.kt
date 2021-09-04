package ru.ezhov.rocket.action.api

/**
 * UI action builder
 */
interface RocketActionUi {
    /**
     * Component creation should only happen when this method is called.
     *
     * @return component to display
     */
    fun create(settings: RocketActionSettings): Action

    /**
     * @return action type
     */
    fun type(): String
}