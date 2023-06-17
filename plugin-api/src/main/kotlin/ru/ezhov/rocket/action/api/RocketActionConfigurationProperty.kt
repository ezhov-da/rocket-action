package ru.ezhov.rocket.action.api

/**
 * Action property
 */
interface RocketActionConfigurationProperty {
    /**
     * Must be unique in terms of action
     * @return property key
     */
    fun key(): String

    /**
     * @return property name to display
     */
    fun name(): String

    /**
     * @return property description to display
     */
    fun description(): String

    /**
     * This property prompts the user to fill in, but does not guarantee that it is filled.
     * @return mandatory property.
     */
    fun isRequired(): Boolean

    /**
     * Property to configure
     */
    fun property(): RocketActionPropertySpec
}
