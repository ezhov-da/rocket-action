package ru.ezhov.rocket.action.api.handler

interface RocketActionHandlerCommandContract {
    /**
     * Название команды
     */
    fun commandName(): String

    /**
     * Заголовок команды
     */
    fun title(): String

    /**
     * Описание команды
     */
    fun description(): String

    /**
     * Входные параметры
     */
    fun inputArguments(): List<RocketActionHandlerProperty>

    /**
     * Выходные параметры
     */
    fun outputParams(): List<RocketActionHandlerProperty>
}
