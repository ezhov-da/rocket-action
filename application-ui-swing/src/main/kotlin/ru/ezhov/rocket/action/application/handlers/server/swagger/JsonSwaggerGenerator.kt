package ru.ezhov.rocket.action.application.handlers.server.swagger

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.SpecVersion
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertySpec
import ru.ezhov.rocket.action.application.handlers.server.BASE_API_PATH
import ru.ezhov.rocket.action.application.handlers.server.HEADER_NAME
import ru.ezhov.rocket.action.application.handlers.server.RocketActionHandlerService
import ru.ezhov.rocket.action.application.handlers.server.extendedhandlers.ExtendedRocketActionHandler


class JsonSwaggerGenerator(
    private val rocketActionHandlerService: RocketActionHandlerService,
) : SwaggerGenerator {
    override fun generate(): String {
        val openApi =
            OpenAPI(SpecVersion.V30)
                .paths(createPaths(listOf(constructHandlers())))
        return ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writeValueAsString(openApi)
    }

    private fun createPaths(inputPaths: List<Pair<String, PathItem>>): Paths {
        var paths = Paths()
        inputPaths.forEach { path ->
            paths = paths.addPathItem(path.first, path.second)
        }
        val handlers = rocketActionHandlerService.handlers()
        val sortedHandlers = handlers.sortedBy { it is ExtendedRocketActionHandler }.reversed()

        sortedHandlers.map { handler ->
            val tag = if (handler is ExtendedRocketActionHandler) {
                "Operations"
            } else {
                "Action"
            }

            handler.toPathItem(tag).forEach {
                paths = paths.addPathItem(it.first, it.second)
            }

        }
        return paths
    }

    private fun constructHandlers(): Pair<String, PathItem> =
        Pair(
            "$BASE_API_PATH/{id}/{commandName}",
            PathItem()
                .get(
                    Operation()
                        .operationId("executeGetCommand")
                        .description("Run command")
                        .addTagsItem("Handler")
                        .summary("Run command")
                        .parameters(
                            listOf(
                                Parameter()
                                    .name("id")
                                    .`in`("path")
                                    .schema(StringSchema())
                                    .required(true),
                                Parameter()
                                    .name("commandName")
                                    .`in`("path")
                                    .schema(StringSchema())
                                    .required(true),
                                Parameter()
                                    .name(HEADER_NAME)
                                    .`in`("query")
                                    .schema(StringSchema())
                                    .required(true)
                                    .example("1234"),
                                Parameter()
                                    .name("any")
                                    .`in`("query")
                                    .schema(StringSchema())
                                    .required(false)
                                    .example("1234")

                            )
                        )
                        .responses(constructResponses())
                )
                .post(
                    Operation()
                        .operationId("executePostCommand")
                        .description("Run command")
                        .addTagsItem("Handler")
                        .summary("Run command")
                        .parameters(
                            listOf(
                                headerKeyParameter(),
                                Parameter()
                                    .name("id")
                                    .`in`("path")
                                    .schema(StringSchema())
                                    .required(true),
                                Parameter()
                                    .name("commandName")
                                    .`in`("path")
                                    .schema(StringSchema())
                                    .required(true),
                            )
                        )
                        .requestBody(
                            RequestBody()
                                .required(true)
                                .content(
                                    Content()
                                        .addMediaType(
                                            "application/json",
                                            MediaType()
                                                .schema(
                                                    ObjectSchema()
                                                        .properties(
                                                            mapOf(
                                                                "key" to StringSchema()
                                                                    .description(
                                                                        "Key-value pair specified in a specific action"
                                                                    )
                                                                    .example("value")
                                                            )
                                                        )
                                                )
                                        )
                                )
                        )
                        .responses(constructResponses())
                )
        )

    private fun headerKeyParameter(): Parameter =
        Parameter()
            .name(HEADER_NAME)
            .`in`("header")
            .schema(StringSchema())
            .required(true)
            .example("1234")

    private fun constructResponses(
        successAnswer: Schema<*> = ObjectSchema()
            .properties(
                mapOf(
                    "key" to StringSchema()
                        .description(
                            "Key-value pair"
                        )
                        .example("value")
                )
            )
    ) =
        ApiResponses()
            .addApiResponse(
                "200",
                ApiResponse()
                    .description("Successful response")
                    .content(
                        Content()
                            .addMediaType(
                                "application/json",
                                MediaType()
                                    .schema(successAnswer)
                            )
                    )
            )
            .addApiResponse(
                "400",
                ApiResponse()
                    .description("Bad request")
                    .content(
                        Content()
                            .addMediaType(
                                "application/json",
                                MediaType()
                                    .schema(
                                        ObjectSchema()
                                            .properties(
                                                mapOf(
                                                    "errors" to ArraySchema()
                                                        .description(
                                                            "List of errors"
                                                        )
                                                        .items(
                                                            StringSchema()
                                                                .example("Error")
                                                        )
                                                )
                                            )
                                    )
                            )
                    )
            )
            .addApiResponse(
                "403",
                ApiResponse()
                    .description("Forbidden")
            )
            .addApiResponse(
                "404",
                ApiResponse()
                    .description("Handler not found")
            )
            .addApiResponse(
                "500",
                ApiResponse()
                    .description("Error")
                    .content(
                        Content()
                            .addMediaType(
                                "application/json",
                                MediaType()
                                    .schema(
                                        ObjectSchema()
                                            .properties(
                                                mapOf(
                                                    "message" to StringSchema()
                                                        .description(
                                                            "Runtime error"
                                                        )
                                                        .example("Error")
                                                )
                                            )
                                    )
                            )
                    )

            )

    private fun RocketActionHandler.toPathItem(tag: String): List<Pair<String, PathItem>> {
        return this.contracts().map { contract ->
            val name = "$BASE_API_PATH/${this.id()}/${contract.commandName()}"
            val request = createBody(contract.inputArguments())
            val response = createBody(contract.outputParams())
            val pathItem = PathItem()
                .post(
                    Operation()
                        .operationId("${this.id()}-${contract.commandName()}")
                        .description(contract.description())
                        .addTagsItem(tag)
                        .summary(contract.title())
                        .parameters(
                            listOf(
                                headerKeyParameter(),
                            )
                        )
                        .requestBody(
                            RequestBody()
                                .required(true)
                                .content(
                                    Content()
                                        .addMediaType(
                                            "application/json",
                                            MediaType()
                                                .schema(
                                                    ObjectSchema()
                                                        .properties(request.first)
                                                        .required(request.second)
                                                )
                                        )
                                )
                        )
                        .responses(
                            constructResponses(
                                successAnswer = ObjectSchema()
                                    .properties(response.first)
                                    .required(response.second)
                            )
                        )
                )

            Pair(name, pathItem)
        }
    }

    private fun createBody(
        properties: List<RocketActionHandlerProperty>
    ): Pair<Map<String, Schema<*>>, List<String>> {
        val required = mutableListOf<String>()
        val params = properties.associate { input ->
            val schema = when (val property = input.property()) {
                is RocketActionHandlerPropertySpec.StringPropertySpec -> {
                    if (input.isRequired()) {
                        required.add(input.key().value)
                    }

                    StringSchema()
                        .description("${input.name()}. ${input.description()}")
                        .example(property.defaultValue)
                }

                is RocketActionHandlerPropertySpec.BooleanPropertySpec -> {
                    if (input.isRequired()) {
                        required.add(input.key().value)
                    }

                    BooleanSchema()
                        .description("${input.name()}. ${input.description()}")
                        .example(property.defaultValue)
                }

                is RocketActionHandlerPropertySpec.IntPropertySpec -> {
                    if (input.isRequired()) {
                        required.add(input.key().value)
                    }

                    IntegerSchema()
                    BooleanSchema()
                        .description("${input.name()}. ${input.description()}")
                        .example(property.defaultValue)
                }
            }

            input.key().value to schema
        }

        return Pair(
            params,
            required
        )
    }
}
