package ru.ezhov.rocket.action.api

/**
 * Action settings
 */
interface RocketActionSettings {
    /**
     * @return action ID
     */
    fun id(): String

    /**
     * @return action type
     */
    fun type(): RocketActionType

    /**
     * @return action settings
     */
    fun settings(): Map<String, String>

    /**
     * Used for group actions
     *
     * @return actions
     */
    fun actions(): List<RocketActionSettings>
}
