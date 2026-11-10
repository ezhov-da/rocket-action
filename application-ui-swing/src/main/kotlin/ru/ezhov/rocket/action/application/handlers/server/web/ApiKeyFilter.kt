package ru.ezhov.rocket.action.application.handlers.server.web

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.web.cors.CorsUtils
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeysApplication
import ru.ezhov.rocket.action.application.handlers.server.HEADER_NAME

private val logger = KotlinLogging.logger {}

/**
 * Checks the API key for both the REST controllers and the MCP servlet.
 *
 * A servlet filter is used instead of an MVC interceptor because the MCP
 * endpoint is a separate servlet and is not handled by DispatcherServlet.
 */
class ApiKeyFilter(
    private val apiKeysApplication: ApiKeysApplication,
) : Filter {

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // CORS preflight requests must not require an API key.
        if (CorsUtils.isPreFlightRequest(httpRequest)) {
            chain.doFilter(request, response)
            return
        }

        val keyValue = httpRequest.getHeader(HEADER_NAME)
            ?: httpRequest.getParameter(HEADER_NAME)

        if (keyValue == null) {
            forbidden(httpResponse, keyValue, httpRequest.remoteAddr)
            return
        }

        val apiKey = apiKeysApplication.apiKey(keyValue)
        if (apiKey == null) {
            forbidden(httpResponse, keyValue, httpRequest.remoteAddr)
            return
        }

        logger.info { "Used API Key='${apiKey.description}'. IP='${httpRequest.remoteAddr}'" }
        chain.doFilter(request, response)
    }

    private fun forbidden(
        response: HttpServletResponse,
        keyValue: String?,
        ip: String,
    ) {
        logger.warn { "Forbidden API Key='$keyValue'. IP='$ip'" }
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "text/plain;charset=UTF-8"
        response.writer.write("Forbidden. Check API key '$HEADER_NAME'")
    }
}
