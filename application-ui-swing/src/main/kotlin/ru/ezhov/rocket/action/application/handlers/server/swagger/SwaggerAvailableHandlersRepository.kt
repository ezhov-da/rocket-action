package ru.ezhov.rocket.action.application.handlers.server.swagger

import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import ru.ezhov.rocket.action.application.handlers.server.Server
import ru.ezhov.rocket.action.application.handlers.server.model.AvailableHandler
import ru.ezhov.rocket.action.application.infrastructure.RocketActionComponentCacheFactory
import java.net.URI

class SwaggerAvailableHandlersRepository : AvailableHandlersRepository {
    private val url: (id: String, commandName: String) -> String =
        { id, commandName -> "http://localhost:${Server.port()}/#/Action/$id-$commandName" }


    override fun by(handlerId: String): List<AvailableHandler> =
        RocketActionComponentCacheFactory
            .cache
            .handlers()
            .firstOrNull { it.id() == handlerId }
            ?.let { handler ->
                handler.contracts().map {
                    AvailableHandler(
                        title = it.title(),
                        uri = URI.create(url(handler.id(), it.commandName()))
                    )
                }
            }
            .orEmpty()

}
