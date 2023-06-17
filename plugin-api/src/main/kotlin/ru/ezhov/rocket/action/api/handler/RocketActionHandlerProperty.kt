package ru.ezhov.rocket.action.api.handler

/**
 * Handler property
 */
interface RocketActionHandlerProperty {
    /**
     * Must be unique in the context of the handler
     * @return property key
     */
    fun key(): RocketActionHandlerPropertyKey

    /**
     * @return property name for the handler
     */
    fun name(): String

    /**
     * @return description of the property for the handler
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
    fun property(): RocketActionHandlerPropertySpec
}
