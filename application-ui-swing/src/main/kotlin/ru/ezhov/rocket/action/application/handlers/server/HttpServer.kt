package ru.ezhov.rocket.action.application.handlers.server

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.application.handlers.server.swagger.JsonSwaggerGenerator
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import spark.Filter
import spark.kotlin.Http
import spark.kotlin.RouteHandler
import spark.kotlin.ignite

private val logger = KotlinLogging.logger {}
private const val HEADER_NAME = "X-Rocket-Action-Handler-Key"
private const val HEADER_VALUE = "1234"
const val BASE_API_PATH = "/api/v1/handlers"

@Service
class HttpServer(
    private val generalPropertiesRepository: GeneralPropertiesRepository,
    private val rocketActionHandlerService: RocketActionHandlerService,
    private val objectMapper: ObjectMapper,
) {

    fun port(): Int = generalPropertiesRepository
        .asIntegerOrNull(UsedPropertiesName.HANDLER_SERVER_PORT)
        ?.let { port ->
            logger.info { "Port for server handler initialized. Value=$port " }
            port
        } ?: 4567

    fun run() {
        val http: Http = ignite()
        port().let { port ->
            logger.info { "Server port is '$port'" }
            http.port(port)
        }
        http.staticFiles.location("/swagger");

        disableCors(http)
        swaggerJson(http)
        executeHandlerEndpoint(http)

        logger.info { "Server started. port='${http.port()}' auth-header-name='$HEADER_NAME' auth-header-value='$HEADER_VALUE'" }
    }

    private fun disableCors(http: Http) {
        http.before(CorsFilter.apply(), "*/*")
        http.options("/*") {
            response.type("application/json")

            val accessControlRequestHeaders = request.headers("Access-Control-Request-Headers")
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders)
            }

            val accessControlRequestMethod = request.headers("Access-Control-Request-Method")
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod)
            }

            response.status(200)
            response.body("OK")
        }
    }


    private fun swaggerJson(http: Http) {
        http.get("/swagger.json") {
            response.type("application/json")
            JsonSwaggerGenerator(rocketActionHandlerService).generate()
        }
    }

    private fun executeHandlerEndpoint(http: Http) {
        http.post("$BASE_API_PATH/:id/:commandName") {
            checkKeyAndExecuteBlock(this) {
                val id = params("id")
                val command = params("commandName")
                val body = request.body()

                logger.info { "Handler called. id=$id command=$command body=$body" }

                val map = objectMapper.readValue(body, Map::class.java)
                val handler = rocketActionHandlerService.handlerBy(id)
                val status = handler
                    ?.handle(
                        RocketActionHandlerCommand(
                            commandName = command,
                            arguments = map
                                .map { (k, v) -> k.toString() to v.toString() }
                                .toMap()
                        )
                    )

                when (status) {
                    null -> response.status(404)

                    is RocketActionHandleStatus.Success -> {
                        response.status(200)
                        objectMapper.writeValueAsString(
                            status.values.map { it.key to it.value }.toMap()
                        )
                    }

                    is RocketActionHandleStatus.InvalidInputData -> {
                        response.status(400)
                        objectMapper.writeValueAsString(
                            InvalidInputDataDto(status.errors)
                        )
                    }

                    is RocketActionHandleStatus.Error -> {
                        response.status(500)
                        logger.error(status.cause) { "Error ${status.message}" }

                        objectMapper.writeValueAsString(
                            ErrorDto(message = status.message)
                        )
                    }
                }
            }
        }
    }

    private fun checkKeyAndExecuteBlock(routeHandler: RouteHandler, block: () -> Any): Any =
        routeHandler.request.headers(HEADER_NAME)
            .let { keyValue ->
                if (keyValue != HEADER_VALUE) {
                    routeHandler.response.status(403)
                    "Forbidden"
                } else {
                    block.invoke()
                }
            }
}

data class InvalidInputDataDto(
    val errors: List<String>
)

data class ErrorDto(
    val message: String
)

// https://gist.github.com/zikani03/7c82b34fbbc9a6187e9a
private object CorsFilter {
    private val corsHeaders = HashMap<String, String>()

    init {
        corsHeaders["Access-Control-Allow-Origin"] = "*"
        corsHeaders["Access-Control-Allow-Methods"] = "GET,PUT,POST,DELETE,OPTIONS"
        corsHeaders["Access-Control-Allow-Headers"] =
            "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,$HEADER_NAME"
    }

    fun apply() =
        Filter { request, response ->
            corsHeaders.forEach { (key, value) ->
                response.header(key, value)
            }
        }
}
