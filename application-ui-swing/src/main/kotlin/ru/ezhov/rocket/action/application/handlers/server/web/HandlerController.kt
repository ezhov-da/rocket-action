package ru.ezhov.rocket.action.application.handlers.server.web

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.application.handlers.server.BASE_API_PATH
import ru.ezhov.rocket.action.application.handlers.server.RocketActionHandlerService
import jakarta.servlet.http.HttpServletRequest

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping(BASE_API_PATH)
class HandlerController(
    private val rocketActionHandlerService: RocketActionHandlerService,
    private val objectMapper: ObjectMapper,
) {

    @PostMapping("/{id}/{commandName}")
    fun executePost(
        @PathVariable("id") id: String,
        @PathVariable("commandName") command: String,
        @RequestBody(required = false) body: ByteArray?,
    ): ResponseEntity<Any> {
        val bodyText = body?.toString(Charsets.UTF_8).orEmpty()
        logger.info { "Handler called. id=$id command=$command body=$bodyText" }

        val map = objectMapper.readValue(bodyText, Map::class.java)
        return processRequest(id, command, map)
    }

    @GetMapping("/{id}/{commandName}")
    fun executeGet(
        @PathVariable("id") id: String,
        @PathVariable("commandName") command: String,
        request: HttpServletRequest,
    ): ResponseEntity<Any> {
        val map = request.parameterMap
            .map { (key, value) -> key to value.firstOrNull().orEmpty() }
            .toMap()
        logger.info { "Handler called. id=$id command=$command body=$map" }

        return processRequest(id, command, map)
    }

    private fun processRequest(id: String, command: String, map: Map<*, *>): ResponseEntity<Any> {
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

        return when (status) {
            null -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()

            is RocketActionHandleStatus.Success ->
                ResponseEntity.ok<Any>(status.values)

            is RocketActionHandleStatus.InvalidInputData ->
                ResponseEntity.badRequest().body(InvalidInputDataDto(status.errors))

            is RocketActionHandleStatus.Error -> {
                logger.error(status.cause) { "Error ${status.message}" }
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorDto(message = status.message))
            }
        }
    }
}

data class InvalidInputDataDto(
    val errors: List<String>
)

data class ErrorDto(
    val message: String
)
