package ru.ezhov.rocket.action.application.handlers.server

import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class HttpServerService(
    private val httpServer: HttpServer,
) {

    fun serverUrl(): String = "http://localhost:${httpServer.port()}"
}
