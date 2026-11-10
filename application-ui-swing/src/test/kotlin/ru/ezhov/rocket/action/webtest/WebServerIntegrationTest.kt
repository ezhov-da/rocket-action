package ru.ezhov.rocket.action.webtest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeysApplication
import ru.ezhov.rocket.action.application.handlers.apikey.domain.model.ApiKey
import ru.ezhov.rocket.action.application.handlers.server.BASE_API_PATH
import ru.ezhov.rocket.action.application.handlers.server.HEADER_NAME
import ru.ezhov.rocket.action.application.handlers.server.MCP_PATH
import ru.ezhov.rocket.action.application.handlers.server.RocketActionHandlerService
import ru.ezhov.rocket.action.application.handlers.server.SWAGGER_UI_PATH
import ru.ezhov.rocket.action.application.handlers.server.mcp.McpServerConfig
import ru.ezhov.rocket.action.application.handlers.server.mcp.McpServerService
import ru.ezhov.rocket.action.application.handlers.server.web.HandlerController
import ru.ezhov.rocket.action.application.handlers.server.web.SwaggerController
import ru.ezhov.rocket.action.application.handlers.server.web.WebConfig
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Boots a real (minimal) Spring Boot web application and verifies that the REST
 * controllers run through Spring MVC, that the MCP endpoint is a servlet, that
 * the API key filter protects both and that Swagger UI is served statically.
 *
 * The test configuration is created inside the test method: a top-level (or
 * static nested) `@Configuration` class in the test class path would be picked
 * up by the component scan of the application context used by other tests.
 * The boot is started with an explicit source set, so the configuration is
 * registered without being discovered by scanning.
 */
class WebServerIntegrationTest {

    private var context: ConfigurableApplicationContext? = null

    @AfterEach
    fun tearDown() {
        context?.close()
    }

    @Test
    fun `spring boot web application serves rest controllers, mcp servlet and swagger ui`() {
        val started = SpringApplication(WebTestApplication::class.java).apply {
            webApplicationType = WebApplicationType.SERVLET
            setHeadless(true)
        }.run(
            "--server.port=0",
            "--spring.main.banner-mode=off",
        )
        context = started

        val port = (started as ServletWebServerApplicationContext).webServer.port
        assertThat(port).isPositive()

        val client = HttpClient.newHttpClient()

        fun get(path: String): HttpResponse<String> =
            client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).GET().build(),
                HttpResponse.BodyHandlers.ofString(),
            )

        // `/` redirects to the Swagger UI served as static content.
        val root = get("/")
        assertThat(root.statusCode()).isIn(302, 303, 307)
        assertThat(root.headers().firstValue("Location").orElse("")).contains("swagger-ui")

        // The Swagger UI root URL (the redirect target) and its index also work.
        assertThat(get(SWAGGER_UI_PATH).statusCode()).isEqualTo(200)
        val swaggerUi = get("${SWAGGER_UI_PATH}index.html")
        assertThat(swaggerUi.statusCode()).isEqualTo(200)
        assertThat(swaggerUi.body()).contains("Swagger UI")

        // Swagger document is produced by the @RestController.
        val swaggerJson = get("/swagger.json")
        assertThat(swaggerJson.statusCode()).isEqualTo(200)
        assertThat(swaggerJson.body()).contains("openapi")

        // API key filter protects the REST API.
        assertThat(get("$BASE_API_PATH/id/run?$HEADER_NAME=wrong").statusCode()).isEqualTo(403)
        val success = get("$BASE_API_PATH/id/run?$HEADER_NAME=test-key")
        assertThat(success.statusCode()).isEqualTo(200)
        assertThat(success.body()).contains("\"result\":\"ok\"")

        // The MCP endpoint is a separate servlet behind the same API key filter.
        assertThat(get(MCP_PATH).statusCode()).isEqualTo(403)
    }

    /**
     * An ordinary (not annotated) class: it is registered as the boot source
     * explicitly, so it is never discovered by component scanning, while its
     * `@Bean` methods are still processed as a configuration class.
     */
    @EnableAutoConfiguration
    @Import(WebConfig::class, McpServerConfig::class, HandlerController::class, SwaggerController::class)
    open class WebTestApplication {
        @Bean
        open fun apiKeysApplication(): ApiKeysApplication = TestBeans.apiKeysApplication

        @Bean
        open fun rocketActionHandlerService(): RocketActionHandlerService = TestBeans.handlerService

        @Bean
        open fun mcpServerService(): McpServerService = TestBeans.mcpServerService

        @Bean
        open fun objectMapper(): ObjectMapper = TestBeans.objectMapper
    }

    private object TestBeans {
        val apiKeysApplication: ApiKeysApplication = mockk {
            every { apiKey("test-key") } returns ApiKey("test-key", "test")
            every { apiKey(match { it != "test-key" }) } returns null
        }

        val handlerService: RocketActionHandlerService = mockk {
            every { handlers() } returns emptyList()
            every { handlerBy("id") } returns mockk<RocketActionHandler> {
                every { id() } returns "id"
                every { contracts() } returns emptyList()
                every { handle(any()) } returns RocketActionHandleStatus.Success(mapOf("result" to "ok"))
            }
            every { handlerBy(match { it != "id" }) } returns null
        }

        val mcpServerService: McpServerService = mockk(relaxed = true)

        val objectMapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
    }
}
