package ru.ezhov.rocket.action.api

/**
 * Action settings
 */
interface RocketActionSettings {
    /**
     * @return configurable action id
     */
    fun id(): String

    /**
     * @return configurable action type
     */
    fun type(): String

    /**
     * @return action settings
     */
    fun settings(): Map<String, String>

    /**
     * @return configured child actions
     */
    fun actions(): List<RocketActionSettings>
}