package ru.ezhov.rocket.action.api

import java.awt.Component

/**
 * UI action builder
 */
interface RocketActionUi {
    /**
     * Component creation should only happen when this method is called.
     *
     * @return component to display
     */
    fun create(settings: RocketActionSettings): Component

    /**
     * @return action type
     */
    fun type(): String
}