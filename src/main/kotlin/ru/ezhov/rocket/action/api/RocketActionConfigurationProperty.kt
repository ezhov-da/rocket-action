package ru.ezhov.rocket.action.api

/**
 * Property awaiting action
 */
interface RocketActionConfigurationProperty {
    /**
     * @return property key
     */
    fun key(): String

    /**
     * @return property key
     */
    fun name(): String

    /**
     * @return property description
     */
    fun description(): String

    /**
     * @return mandatory property
     */
    val isRequired: Boolean
}