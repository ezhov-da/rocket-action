package ru.ezhov.rocket.action.api

/**
 * Настройки действия
 */
interface RocketActionSettings {
    /**
     * @return идентификатор действия
     */
    fun id(): String

    /**
     * @return тип действия
     */
    fun type(): RocketActionType

    /**
     * @return настройки действия
     */
    fun settings(): Map<String, String>

    /**
     * Используется для групповых действий
     *
     * @return действия
     */
    fun actions(): List<RocketActionSettings>
}
