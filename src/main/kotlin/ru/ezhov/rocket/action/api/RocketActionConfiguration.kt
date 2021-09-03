package ru.ezhov.rocket.action.api

/**
 * Configuring actions
 */
interface RocketActionConfiguration {
    /**
     * @return configurable action type
     */
    fun type(): String

    /**
     * @return configurable action description
     */
    fun description(): String

    /**
     * @return list of action properties to configure
     */
    fun properties(): List<RocketActionConfigurationProperty>
}