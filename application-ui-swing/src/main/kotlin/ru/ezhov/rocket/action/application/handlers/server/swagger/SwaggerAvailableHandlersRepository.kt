package ru.ezhov.rocket.action.application.handlers.server.swagger

import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.application.core.domain.RocketActionComponentCache
import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import ru.ezhov.rocket.action.application.handlers.server.HttpServerService
import ru.ezhov.rocket.action.application.handlers.server.model.AvailableHandler
import java.net.URI

@Component
class SwaggerAvailableHandlersRepository(
    private val httpServerService: HttpServerService,
    private val rocketActionComponentCache: RocketActionComponentCache,
) : AvailableHandlersRepository {
    private val url: (id: String, commandName: String) -> String =
        { id, commandName -> "${httpServerService.serverUrl()}/#/Action/$id-$commandName" }


    override fun by(handlerId: String): List<AvailableHandler> =
        rocketActionComponentCache
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
