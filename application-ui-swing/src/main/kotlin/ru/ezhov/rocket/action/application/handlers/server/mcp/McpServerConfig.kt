package ru.ezhov.rocket.action.application.handlers.server.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.json.McpJsonMapper
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.ezhov.rocket.action.application.handlers.server.MCP_PATH

/**
 * Registers the MCP streamable HTTP transport as a regular servlet next to the
 * Spring MVC dispatcher. The transport is backed by the MCP SDK's
 * [HttpServletStreamableServerTransportProvider] and is connected to the
 * [McpServerService] tools.
 */
@Configuration
open class McpServerConfig {

    @Bean
    open fun mcpJsonMapper(objectMapper: ObjectMapper): McpJsonMapper =
        JacksonMcpJsonMapper(objectMapper)

    @Bean
    open fun mcpTransportProvider(
        mcpJsonMapper: McpJsonMapper,
    ): HttpServletStreamableServerTransportProvider =
        HttpServletStreamableServerTransportProvider.builder()
            .jsonMapper(mcpJsonMapper)
            .mcpEndpoint(MCP_PATH)
            .maxRequestSize(MAX_REQUEST_SIZE)
            .build()

    @Bean
    open fun mcpServlet(
        transportProvider: HttpServletStreamableServerTransportProvider,
    ): ServletRegistrationBean<HttpServletStreamableServerTransportProvider> =
        ServletRegistrationBean(transportProvider, MCP_PATH).apply {
            isAsyncSupported = true
            setLoadOnStartup(1)
        }

    private companion object {
        private const val MAX_REQUEST_SIZE = 1024 * 1024
    }
}
