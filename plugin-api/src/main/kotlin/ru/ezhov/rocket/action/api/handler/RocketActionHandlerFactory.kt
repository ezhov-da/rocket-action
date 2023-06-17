package ru.ezhov.rocket.action.api.handler

/**
 * Interface to be implemented [ru.ezhov.rocket.action.api.RocketAction] to support handlers
 */
interface RocketActionHandlerFactory {
    /**
     * The handler is obtained once
     *
     * May be null if failed to create
     */
    fun handler(): RocketActionHandler?
}
