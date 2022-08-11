package ru.ezhov.rocket.action.api.handler

/**
 * Обработчик действий.
 * Позволяет расширить набор действий не ограничиваясь UI интерфейсом.
 */
interface RocketActionHandler {
    /**
     * Идентификатор обработчика
     */
    fun id(): String

    /**
     * Контракты поддерживаемые обработчиком
     */
    fun contracts(): List<RocketActionHandlerCommandContract>

    /**
     * Обработка команды
     */
    fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus
}
