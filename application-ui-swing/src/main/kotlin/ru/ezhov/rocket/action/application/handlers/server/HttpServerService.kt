package ru.ezhov.rocket.action.application.handlers.server

import mu.KotlinLogging
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName

private val logger = KotlinLogging.logger {}

@Service
class HttpServerService(
    private val applicationContext: ApplicationContext,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
) {

    /**
     * Port the embedded Spring Boot server is actually listening on. Falls back
     * to the configured value when the application runs without a web server
     * (for example, in tests).
     */
    fun port(): Int {
        val context = applicationContext
        if (context is ServletWebServerApplicationContext) {
            val port = context.webServer.port
            if (port > 0) {
                return port
            }
        }

        return generalPropertiesRepository
            .asIntegerOrNull(UsedPropertiesName.HANDLER_SERVER_PORT)
            ?.also { port -> logger.info { "Port for server handler initialized. Value=$port" } }
            ?: DEFAULT_PORT
    }

    fun serverUrl(): String = "http://localhost:${port()}"

    private companion object {
        private const val DEFAULT_PORT = 4567
    }
}
