package ru.ezhov.rocket.action.application.handlers.server.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.modelcontextprotocol.json.McpJsonMapper
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper
import io.modelcontextprotocol.server.McpSyncServer
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.AtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Проверка пункта 9.3 целиком: действие, отмеченное для MCP, появляется в списке
 * инструментов после перезагрузки, а снятая отметка убирает инструмент.
 *
 * MCP-сервер настоящий (реестр инструментов живёт в нём), подменено только
 * хранилище действий, поэтому тест не трогает пользовательские
 * JSON-файлы в `.rocket-action` и не ходит в сеть.
 */
internal class McpToolListRefreshTest {

    private var service: McpServerService? = null

    @AfterEach
    fun tearDown() {
        service?.close()
    }

    @Test
    fun `tool list follows the mark and unmark of an action`() {
        val published = AtomicBoolean(false)
        val action = atomicAction()

        val atomicRepository = object : AtomicActionRepository {
            override fun save(atomicAction: AtomicAction) = Unit

            override fun all(): List<AtomicAction> = listOf(action.copy(isTool = published.get()))

            override fun byId(id: String): AtomicAction? = all().firstOrNull { it.id == id }

            override fun delete(id: String) = Unit
        }
        val chainRepository = object : ChainActionRepository {
            override fun save(chainAction: ChainAction) = Unit

            override fun all(): List<ChainAction> = emptyList()

            override fun delete(id: String) = Unit
        }

        val objectMapper = ObjectMapper()
        val jsonMapper: McpJsonMapper = JacksonMcpJsonMapper(objectMapper)
        val properties = mockk<GeneralPropertiesRepository>()
        every { properties.asStringOrNull(any()) } returns "test"

        val mcpService = McpServerService(
            mcpExecuteActionRocketActionHandler = McpActionRocketActionService(
                chainActionService = ChainActionService(chainRepository),
                atomicActionService = AtomicActionService(atomicRepository),
                actionExecutor = mockk<ActionExecutor>(relaxed = true),
            ),
            generalPropertiesRepository = properties,
            objectMapper = objectMapper,
            transportProvider = HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .mcpEndpoint("/mcp")
                .maxRequestSize(1024 * 1024)
                .build(),
            mcpJsonMapper = jsonMapper,
        )
        service = mcpService
        mcpService.start()

        val server = mcpService.server()
        val toolName = McpToolName.generated(action.id, action.name)

        // nothing is marked: no tools
        assertThat(server.listTools()).isEmpty()

        // mark the action for MCP and reload the tools
        published.set(true)
        mcpService.refreshTools()
        assertThat(server.listTools().map { it.name() }).containsExactly(toolName)

        // unmark the action and reload again the tools
        published.set(false)
        mcpService.refreshTools()
        assertThat(server.listTools()).isEmpty()
    }

    private fun McpServerService.server(): McpSyncServer {
        val field = McpServerService::class.java.getDeclaredField("server")
        field.isAccessible = true
        return field.get(this) as? McpSyncServer ?: error("MCP server was not started")
    }

    private fun atomicAction() = AtomicAction(
        id = ACTION_ID,
        name = "Tool Action",
        description = "Description",
        contractType = ContractType.IN_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = "return 'result'",
    )

    private companion object {
        const val ACTION_ID = "tool-id"
    }
}
