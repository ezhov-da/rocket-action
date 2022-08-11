package ru.ezhov.rocket.action.api.handler

/**
 * Интерфейс, который необходимо реализовать [ru.ezhov.rocket.action.api.RocketAction] для поддержки обработчиков
 */
interface RocketActionHandlerFactory {
    /**
     * Обработчик получается один раз
     *
     * Может быть null если не удалось создать
     */
    fun handler(): RocketActionHandler?
}
