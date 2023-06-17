package ru.ezhov.rocket.action.api.handler

/**
 * Action handler.
 * Allows you to expand the set of actions not limited to the UI interface.
 */
interface RocketActionHandler {
    /**
     * Handler ID
     */
    fun id(): String

    /**
     * Contracts supported by handler
     */
    fun contracts(): List<RocketActionHandlerCommandContract>

    /**
     * Command processing
     */
    fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus
}
