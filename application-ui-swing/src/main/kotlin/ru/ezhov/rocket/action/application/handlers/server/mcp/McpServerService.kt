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
import ru.ezhov.rocket.action.application.MenuContentReplacedDomainEvent
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

    // Tool rebuilds run off the Swing thread and are serialised.
    private val refreshScheduler = McpRefreshScheduler(refresh = ::refreshTools)

    init {
        // Меню пересобирается после перезагрузки настроек/плагинов,
        // в этот момент и обновляем список MCP-инструментов.
        DomainEventFactory.subscriberRegistrar.subscribe(
            object : DomainEventSubscriber {
                override fun handleEvent(event: DomainEvent) {
                    val scope = (event as? MenuContentReplacedDomainEvent)?.scope
                    refreshScheduler.submit(scope)
                }

                override fun subscribedToEventType(): List<Class<*>> = listOf(
                    MenuContentReplacedDomainEvent::class.java,
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
        McpToolName.published(mcpExecuteActionRocketActionHandler.all()).map { action ->
            toolSpecification(action)
        }

    private fun toolSpecification(
        action: McpAction,
    ): McpServerFeatures.SyncToolSpecification =
        McpServerFeatures.SyncToolSpecification.builder()
            .tool(
                McpSchema.Tool.builder(
                    action.toolName,
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

    private companion object {
        private const val SERVER_INFO_NAME = "rocket-action"
        private const val UNKNOWN_VERSION = "unknown"
    }
}
