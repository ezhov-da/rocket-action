package ru.ezhov.rocket.action.api

/**
 * Свойство действия
 */
interface RocketActionConfigurationProperty {
    /**
     * Должен быть уникальный в разрезе действия
     * @return ключ свойства
     */
    fun key(): RocketActionConfigurationPropertyKey

    /**
     * @return название свойства для отображения
     */
    fun name(): String

    /**
     * @return описание свойства для отображения
     */
    fun description(): String

    /**
     * Это свойство подсказывает пользователю о необходимости заполнения, но не гарантирует заполненность.
     * @return обязательность заполнения свойства.
     */
    fun isRequired(): Boolean

    /**
     * Свойство для конфигурирования
     */
    fun property(): RocketActionPropertySpec
}