package ru.ezhov.rocket.action.application.handlers.server.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.json.McpJsonMapper
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.McpServerFeatures
import io.modelcontextprotocol.server.McpSyncServer
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import io.modelcontextprotocol.spec.McpSchema
import mu.KotlinLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.application.CreateMenuDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName

private val logger = KotlinLogging.logger {}

/**
 * MCP-сервер приложения.
 *
 * Публикуются атомарные действия и цепочки действий, отмеченные как используемые для MCP.
 * При вызове инструмента обработчик ищется заново по id, поэтому перезагрузка
 * плагинов не приводит к вызову устаревшего экземпляра обработчика.
 *
 * Транспорт ([HttpServletStreamableServerTransportProvider]) регистрируется как
 * сервлет в [McpServerConfig]; здесь к нему подключается сам MCP-сервер.
 */
@Service
class McpServerService(
    private val mcpExecuteActionRocketActionHandler: McpActionRocketActionService,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
    private val objectMapper: ObjectMapper,
    private val transportProvider: HttpServletStreamableServerTransportProvider,
    private val mcpJsonMapper: McpJsonMapper,
) : InitializingBean, DisposableBean {

    @Volatile
    private var server: McpSyncServer? = null

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

                override fun subscribedToEventType(): List<Class<*>> = listOf(
                    CreateMenuDomainEvent::class.java,

                    ChainActionCreatedDomainEvent::class.java,
                    ChainActionUpdatedDomainEvent::class.java,
                    ChainActionDeletedDomainEvent::class.java,

                    AtomicActionCreatedDomainEvent::class.java,
                    AtomicActionUpdatedDomainEvent::class.java,
                    AtomicActionDeletedDomainEvent::class.java,
                )
            }
        )
    }

    override fun afterPropertiesSet() {
        start()
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

    override fun destroy() {
        close()
    }

    fun close() {
        server?.closeGracefully()
        server = null
    }

    private fun build(): McpSyncServer =
        McpServer.sync(transportProvider)
            .serverInfo(SERVER_INFO_NAME, version())
            .instructions("Rocket Action handlers and commands exposed as MCP tools")
            .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
            .jsonMapper(mcpJsonMapper)
            .jsonSchemaValidator(DefaultJsonSchemaValidator(objectMapper))
            .tools(toolSpecifications())
            .build()

    private fun version(): String = generalPropertiesRepository
        .asStringOrNull(UsedPropertiesName.VERSION)
        ?: UNKNOWN_VERSION

    private fun toolSpecifications(): List<McpServerFeatures.SyncToolSpecification> =
        mcpExecuteActionRocketActionHandler.all().map { action ->
            toolSpecification(action)
        }

    private fun toolSpecification(
        action: McpAction,
    ): McpServerFeatures.SyncToolSpecification =
        McpServerFeatures.SyncToolSpecification.builder()
            .tool(
                McpSchema.Tool.builder(
                    toolName(action.action.id(), action.action.name()),
                    inputSchema(action.contractType),
                )
                    .title(action.action.name())
                    .description(action.action.description())
                    .build()
            )
            .callHandler { _, request -> execute(action.action.id(), request) }
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
    private fun inputSchema(contractType: ContractType?): Map<String, Any> {
        val properties = linkedMapOf<String, Any>()
        val required = mutableListOf<String>()

        val schema = when (contractType) {
            ContractType.IN_OUT, ContractType.IN_UNIT -> {
                required.add("input")
                linkedMapOf<String, Any>("type" to "string")
            }

            ContractType.UNIT_OUT, ContractType.UNIT_UNIT, null -> linkedMapOf()
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
