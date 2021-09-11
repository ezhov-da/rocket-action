package ru.ezhov.rocket.action.api

/**
 * Конфигурация действия
 */
interface RocketActionConfiguration {
    /**
     * Уникальное значение в рамках всех загруженных действия
     *
     * @return тип действия
     */
    fun type(): RocketActionType

    /**
     * Название действия для отображения
     *
     * @return название
     */
    fun name(): String

    /**
     * Описание действия для отображения
     *
     * @return описание
     */
    fun description(): String

    /**
     * Свойства действия для конфигурирования
     *
     * @return свойства действия
     */
    fun properties(): List<RocketActionConfigurationProperty>
}