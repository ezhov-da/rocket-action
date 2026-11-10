package ru.ezhov.rocket.action.application.handlers.server.web

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import ru.ezhov.rocket.action.application.handlers.server.RocketActionHandlerService
import ru.ezhov.rocket.action.application.handlers.server.swagger.JsonSwaggerGenerator

@RestController
class SwaggerController(
    private val rocketActionHandlerService: RocketActionHandlerService,
) {

    @GetMapping("/swagger.json", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun swaggerJson(): String =
        JsonSwaggerGenerator(rocketActionHandlerService).generate()
}
