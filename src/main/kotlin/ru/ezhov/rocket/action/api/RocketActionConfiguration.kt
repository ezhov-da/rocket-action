package ru.ezhov.rocket.action.api

import javax.swing.Icon

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
     * @return список ключей значение первого из которых
     * не пустое и не null будет использоваться для отображения настроек
     */
    fun asString(): List<RocketActionConfigurationPropertyKey>

    /**
     * Свойства действия для конфигурирования
     *
     * @return свойства действия
     */
    fun properties(): List<RocketActionConfigurationProperty>

    /**
     * Иконка конфигурации
     *
     * @return иконка
     */
    fun icon(): Icon?
}