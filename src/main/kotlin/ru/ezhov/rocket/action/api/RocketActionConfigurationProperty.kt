package ru.ezhov.rocket.action.api

/**
 * Property awaiting action
 */
interface RocketActionConfigurationProperty {
    /**
     * @return property name
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