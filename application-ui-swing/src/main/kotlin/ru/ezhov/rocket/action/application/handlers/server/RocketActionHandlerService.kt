package ru.ezhov.rocket.action.application.handlers.server

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.application.core.domain.RocketActionComponentCache
import ru.ezhov.rocket.action.application.handlers.server.extendedhandlers.ExtendedRocketActionHandler

@Service
class RocketActionHandlerService(
    private val rocketActionComponentCache: RocketActionComponentCache,
    private val extendedRocketActionHandlers: List<ExtendedRocketActionHandler>,
) {
    fun handlers(): List<RocketActionHandler> =
        rocketActionComponentCache.handlers() + extendedRocketActionHandlers

    fun handlerBy(id: String): RocketActionHandler? =
        (rocketActionComponentCache.handlers() + extendedRocketActionHandlers).firstOrNull { it.id() == id }
}
