package ru.ezhov.rocket.action.application.handlers.server.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.common.McpTransportContext
import io.modelcontextprotocol.json.McpJsonMapper
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.McpStatelessServerFeatures
import io.modelcontextprotocol.server.McpStatelessServerHandler
import io.modelcontextprotocol.server.McpStatelessSyncServer
import io.modelcontextprotocol.spec.McpError
import io.modelcontextprotocol.spec.McpSchema
import io.modelcontextprotocol.spec.McpStatelessServerTransport
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.application.CreateMenuDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName

private val logger = KotlinLogging.logger {}

private const val MAX_REQUEST_SIZE = 1024 * 1024

data class McpHttpResult(
    val status: Int,
    val body: String? = null,
)

/**
 * Адаптер "stateless" транспорта MCP поверх Spark (javax.servlet/Jetty 9).
 *
 * Формат обмена: один POST = один JSON-RPC request/notification, ответ - обычный JSON.
 * Такой транспорт не требует Jakarta Servlet API, сессий и SSE, поэтому работает
 * на том же Spark-сервере, что и остальной HTTP API приложения.
 */
class SparkStatelessMcpTransport(
    private val jsonMapper: McpJsonMapper,
    private val maxRequestSize: Int = MAX_REQUEST_SIZE,
) : McpStatelessServerTransport {

    @Volatile
    private var handler: McpStatelessServerHandler? = null

    override fun setMcpHandler(mcpHandler: McpStatelessServerHandler) {
        handler = mcpHandler
    }

    override fun closeGracefully(): Mono<Void> {
        handler = null
        return Mono.empty()
    }

    fun handle(body: String): McpHttpResult {
        if (body.toByteArray(Charsets.UTF_8).size > maxRequestSize) {
            return error(413, McpSchema.ErrorCodes.INVALID_REQUEST, "Payload too large")
        }

        val message = try {
            McpSchema.deserializeJsonRpcMessage(jsonMapper, body)
        } catch (e: Exception) {
            logger.warn(e) { "Can't parse MCP message" }
            return error(400, McpSchema.ErrorCodes.PARSE_ERROR, "Invalid message format")
        }

        val currentHandler = handler
            ?: return error(503, McpSchema.ErrorCodes.INTERNAL_ERROR, "MCP server is not started")

        return try {
            when (message) {
                is McpSchema.JSONRPCRequest -> {
                    val response = currentHandler.handleRequest(McpTransportContext.EMPTY, message).block()
                    McpHttpResult(200, jsonMapper.writeValueAsString(response))
                }

                is McpSchema.JSONRPCNotification -> {
                    currentHandler.handleNotification(McpTransportContext.EMPTY, message).block()
                    McpHttpResult(202)
                }

                else -> error(400, McpSchema.ErrorCodes.INVALID_REQUEST, "Only requests or notifications are accepted")
            }
        } catch (e: Exception) {
            logger.error(e) { "MCP message handling failed" }
            error(500, McpSchema.ErrorCodes.INTERNAL_ERROR, "Failed to handle message: ${e.message}")
        }
    }

    private fun error(httpStatus: Int, code: Int, message: String): McpHttpResult {
        val jsonRpcError = McpError.builder(code).message(message).build().jsonRpcError
        val body = linkedMapOf<String, Any?>(
            "jsonrpc" to "2.0",
            "id" to null,
            "error" to linkedMapOf<String, Any?>(
                "code" to jsonRpcError.code(),
                "message" to jsonRpcError.message(),
            ),
        )

        return McpHttpResult(httpStatus, jsonMapper.writeValueAsString(body))
    }
}

/**
 * MCP-сервер приложения.
 *
 * Каждый контракт каждого [ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction]
 * публикуется как отдельный MCP-инструмент. При вызове инструмента обработчик
 * ищется заново по id, поэтому перезагрузка плагинов не приводит к вызову
 * устаревшего экземпляра обработчика.
 */
@Service
class McpServerService(
    private val mcpExecuteActionRocketActionHandler: McpActionRocketActionService,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
    private val objectMapper: ObjectMapper,
) {
    private val jsonMapper: McpJsonMapper = JacksonMcpJsonMapper(objectMapper)
    private val transport = SparkStatelessMcpTransport(jsonMapper)

    @Volatile
    private var server: McpStatelessSyncServer? = null

    init {
        // Меню пересобирается после перезагрузки настроек/плагинов,
        // в этот момент и обновляем список MCP-инструментов.
        DomainEventFactory.subscriberRegistrar.subscribe(
            object : DomainEventSubscriber {
                override fun handleEvent(event: DomainEvent) {
                    try {
                        refreshTools()
                    } catch (e: Exception) {
                        logger.error(e) { "Can't refresh MCP tools" }
                    }
                }

                override fun subscribedToEventType(): List<Class<*>> = listOf(CreateMenuDomainEvent::class.java)
            }
        )
    }

    fun handle(body: String): McpHttpResult {
        ensureStarted()
        return transport.handle(body)
    }

    fun start() {
        if (server != null) {
            return
        }

        synchronized(this) {
            if (server != null) {
                return
            }

            val started = build()
            server = started

            val serverInfo = started.serverInfo
            logger.info {
                "MCP server started. server-info='${serverInfo.name()}', " +
                    "tools=${started.listTools().map { tool -> tool.name() }}"
            }
        }
    }

    /**
     * Перестраивает список инструментов под текущий набор обработчиков.
     */
    fun refreshTools() {
        val current = server ?: return

        val actual = toolSpecifications()
        val actualNames = actual.map { it.tool().name() }.toSet()

        current.listTools()
            .map { it.name() }
            .filterNot { it in actualNames }
            .forEach(current::removeTool)

        actual.forEach(current::addTool)

        logger.info { "MCP tools refreshed. count='${actual.size}'" }
    }

    fun close() {
        server?.closeGracefully()
        server = null
    }

    private fun ensureStarted() {
        if (server == null) {
            start()
        }
    }

    private fun build(): McpStatelessSyncServer =
        McpServer.sync(transport)
            .serverInfo(SERVER_INFO_NAME, version())
            .instructions("Rocket Action handlers and commands exposed as MCP tools")
            // stateless-транспорт не может присылать listChanged-нотификации
            .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
            .jsonMapper(jsonMapper)
            .jsonSchemaValidator(DefaultJsonSchemaValidator(objectMapper))
            .tools(toolSpecifications())
            .build()

    private fun version(): String = generalPropertiesRepository
        .asStringOrNull(UsedPropertiesName.VERSION)
        ?: UNKNOWN_VERSION

    private fun toolSpecifications(): List<McpStatelessServerFeatures.SyncToolSpecification> =
        mcpExecuteActionRocketActionHandler.all().map { action ->
            toolSpecification(action)
        }

    private fun toolSpecification(
        action: AtomicAction,
    ): McpStatelessServerFeatures.SyncToolSpecification =
        McpStatelessServerFeatures.SyncToolSpecification.builder()
            .tool(
                McpSchema.Tool.builder(toolName(action.id(), action.name()), inputSchema(action))
                    .title(action.name())
                    .description(action.description())
                    .build()
            )
            .callHandler { _, request -> execute(action.id, request) }
            .build()

    private fun execute(
        actionId: String,
        request: McpSchema.CallToolRequest,
    ): McpSchema.CallToolResult {
        val args = request.arguments().orEmpty().values.firstOrNull()?.toString()

        val result = mcpExecuteActionRocketActionHandler.execute(
            actionId,
            args
        )

        logger.info {
            "MCP tool called. handler='$actionId' args=${args}"
        }

        return when (result) {
            is RocketActionHandleStatus.Success -> McpSchema.CallToolResult.builder()
                .addTextContent(objectMapper.writeValueAsString(result.values))
                .build()

            is RocketActionHandleStatus.InvalidInputData -> errorResult(result.errors.joinToString("\n"))

            is RocketActionHandleStatus.Error -> {
                logger.error(result.cause) { "Handler '$actionId' error: ${result.message}" }
                errorResult(result.message)
            }
        }
    }

    private fun errorResult(message: String): McpSchema.CallToolResult =
        McpSchema.CallToolResult.builder()
            .addTextContent(message)
            .isError(true)
            .build()

    /**
     * Input-параметры контракта обработчика -> JSON Schema инструмента.
     */
    private fun inputSchema(action: AtomicAction): Map<String, Any> {
        val properties = linkedMapOf<String, Any>()
        val required = mutableListOf<String>()

        val schema = when (action.contractType) {
            ContractType.IN_OUT, ContractType.IN_UNIT -> {
                required.add("input")
                linkedMapOf<String, Any>("type" to "string")
            }

            ContractType.UNIT_OUT, ContractType.UNIT_UNIT -> linkedMapOf()
        }

        properties["input"] = schema

        return linkedMapOf(
            "type" to "object",
            "properties" to properties,
            "additionalProperties" to false,
        )
    }

    private fun String.toTranslit(): String {
        val charMap = mapOf(
            'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d",
            'е' to "e", 'ё' to "yo", 'ж' to "zh", 'з' to "z", 'и' to "i",
            'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n",
            'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t",
            'у' to "u", 'ф' to "f", 'х' to "h", 'ц' to "ts", 'ч' to "ch",
            'ш' to "sh", 'щ' to "sch", 'ъ' to "", 'ы' to "y", 'ь' to "",
            'э' to "e", 'ю' to "yu", 'я' to "ya",
            'А' to "A", 'Б' to "B", 'В' to "V", 'Г' to "G", 'Д' to "D",
            'Е' to "E", 'Ё' to "Yo", 'Ж' to "Zh", 'З' to "Z", 'И' to "I",
            'Й' to "Y", 'К' to "K", 'Л' to "L", 'М' to "M", 'Н' to "N",
            'О' to "O", 'П' to "P", 'Р' to "R", 'С' to "S", 'Т' to "T",
            'У' to "U", 'Ф' to "F", 'Х' to "H", 'Ц' to "Ts", 'Ч' to "Ch",
            'Ш' to "Sh", 'Щ' to "Sch", 'Ъ' to "", 'Ы' to "Y", 'Ь' to "",
            'Э' to "E", 'Ю' to "Yu", 'Я' to "Ya"
        )

        return buildString {
            for (char in this@toTranslit) {
                append(charMap[char] ?: char)
            }
        }
    }


    private fun toolName(handlerId: String, commandName: String): String =
        (commandName.toTranslit() + "__" + handlerId.replace("-", ""))
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
            .take(MAX_TOOL_NAME_LENGTH)

    private companion object {
        private const val SERVER_INFO_NAME = "rocket-action"
        private const val UNKNOWN_VERSION = "unknown"
        private const val MAX_TOOL_NAME_LENGTH = 128
    }
}
