package ru.ezhov.rocket.action.application.handlers.server.web

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeysApplication
import ru.ezhov.rocket.action.application.handlers.server.BASE_API_PATH
import ru.ezhov.rocket.action.application.handlers.server.HEADER_NAME
import ru.ezhov.rocket.action.application.handlers.server.MCP_PATH
import ru.ezhov.rocket.action.application.handlers.server.SWAGGER_UI_LOCATION
import ru.ezhov.rocket.action.application.handlers.server.SWAGGER_UI_PATH

/**
 * Web infrastructure common to the REST controllers and the MCP servlet.
 *
 * MVC auto-configuration stays enabled (no `@EnableWebMvc`), so the Spring Boot
 * conventions for message converters and other auto-configured MVC features are
 * kept; only the Swagger UI static resources are added explicitly.
 */
@Configuration
open class WebConfig(
    private val apiKeysApplication: ApiKeysApplication,
) : WebMvcConfigurer {

    /**
     * Swagger UI is served from the classpath folder `/swagger` under `/swagger-ui/`.
     * The location is registered explicitly because the path is otherwise
     * handled by the swagger-ui webjar handler.
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("$SWAGGER_UI_PATH**")
            .addResourceLocations(SWAGGER_UI_LOCATION)
    }

    /**
     * Swagger UI used to be served at the root URL; keep `/` and the Swagger UI
     * directory URL working.
     */
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "${SWAGGER_UI_PATH}index.html")
        registry.addViewController(SWAGGER_UI_PATH).setViewName("forward:index.html")
    }

    @Bean
    open fun corsFilterRegistration(): FilterRegistrationBean<CorsFilter> {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedMethods = listOf("GET", "PUT", "POST", "DELETE", "OPTIONS")
            allowedHeaders = listOf(
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "Content-Length",
                "Accept",
                "Origin",
                HEADER_NAME,
            )
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }

        return FilterRegistrationBean(CorsFilter(source)).apply {
            order = Ordered.HIGHEST_PRECEDENCE
            addUrlPatterns("/*")
        }
    }

    /**
     * The API key filter protects both the REST API (handled by
     * DispatcherServlet) and the MCP endpoint (a separate servlet).
     */
    @Bean
    open fun apiKeyFilterRegistration(): FilterRegistrationBean<ApiKeyFilter> =
        FilterRegistrationBean(ApiKeyFilter(apiKeysApplication)).apply {
            order = Ordered.HIGHEST_PRECEDENCE + 10
            addUrlPatterns("$BASE_API_PATH/*", MCP_PATH)
        }
}
