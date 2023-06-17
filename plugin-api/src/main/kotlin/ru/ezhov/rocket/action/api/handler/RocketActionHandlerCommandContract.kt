package ru.ezhov.rocket.action.api.handler

interface RocketActionHandlerCommandContract {
    /**
     * Command name
     */
    fun commandName(): String

    /**
     * Command title
     */
    fun title(): String

    /**
     * Command description
     */
    fun description(): String

    /**
     * Input parameters
     */
    fun inputArguments(): List<RocketActionHandlerProperty>

    /**
     * Output parameters
     */
    fun outputParams(): List<RocketActionHandlerProperty>
}
