package ru.ezhov.rocket.action.api

import java.awt.Component

/**
 * Created and ready to work action
 */
interface RocketAction {
    /**
     * Specifies whether this action should be displayed in search results

     * @return true if the action should be reflected in the search results
     */
    fun contains(search: String): Boolean

    /**
     * Whether the settings of the action have changed and whether it needs to be recreated.
     * Used to cache the action
     */
    fun isChanged(actionSettings: RocketActionSettings): Boolean

    /**
     * The action component to display.
     * Important. This method must not create a component.
     * The component created by the [RocketActionFactoryUi.create] method should be returned.
     */
    fun component(): Component
}
