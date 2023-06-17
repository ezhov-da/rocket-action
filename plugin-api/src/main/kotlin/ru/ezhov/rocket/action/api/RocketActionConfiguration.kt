package ru.ezhov.rocket.action.api

import javax.swing.Icon

/**
 * Action Configuration
 */
interface RocketActionConfiguration {
    /**
     * Unique value within all loaded activities
     *
     * @return action type
     */
    fun type(): RocketActionType

    /**
     * Action name to display
     *
     * @return name
     */
    fun name(): String

    /**
     * Action description to display
     *
     * @return description
     */
    fun description(): String

    /**
     * @return list of keys the value of the first of which
     * is not empty and not null will be used to display the settings
     */
    fun asString(): List<String>

    /**
     * Action properties for configuration
     *
     * @return action properties
     */
    fun properties(): List<RocketActionConfigurationProperty>

    /**
     * Configuration icon
     *
     * @return icon
     */
    fun icon(): Icon?
}
