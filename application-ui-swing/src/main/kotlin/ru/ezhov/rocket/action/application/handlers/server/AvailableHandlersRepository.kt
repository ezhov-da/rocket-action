package ru.ezhov.rocket.action.application.handlers.server

import ru.ezhov.rocket.action.application.handlers.server.model.AvailableHandler

interface AvailableHandlersRepository {
    fun by(handlerId: String): List<AvailableHandler>
}
