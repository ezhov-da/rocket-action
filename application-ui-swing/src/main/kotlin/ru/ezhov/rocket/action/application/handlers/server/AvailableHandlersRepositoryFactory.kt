package ru.ezhov.rocket.action.application.handlers.server

import ru.ezhov.rocket.action.application.handlers.server.swagger.SwaggerAvailableHandlersRepository

object AvailableHandlersRepositoryFactory {
    val repository = SwaggerAvailableHandlersRepository()
}
