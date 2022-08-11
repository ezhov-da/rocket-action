package ru.ezhov.rocket.action.api.handler

/**
 * Свойство обработчика
 */
interface RocketActionHandlerProperty {
    /**
     * Должен быть уникальный в разрезе обработчика
     * @return ключ свойства
     */
    fun key(): RocketActionHandlerPropertyKey

    /**
     * @return название свойства для обработчика
     */
    fun name(): String

    /**
     * @return описание свойства для обработчика
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
    fun property(): RocketActionHandlerPropertySpec
}
