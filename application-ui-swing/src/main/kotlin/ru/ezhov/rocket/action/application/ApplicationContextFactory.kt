package ru.ezhov.rocket.action.application

import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext

object ApplicationContextFactory {
    @Volatile
    private var appContext: ConfigurableApplicationContext? = null

    /**
     * Starts the Spring Boot application with the embedded servlet container.
     * Used by the desktop entry point.
     */
    fun start(args: Array<String>): ConfigurableApplicationContext =
        contextOrCreate(type = WebApplicationType.SERVLET, headless = true, args = args)

    /**
     * Returns the application context, creating a non-web context when the
     * application has not been started yet (used by tests and manual harnesses).
     */
    fun context(): ApplicationContext =
        contextOrCreate(type = WebApplicationType.NONE, headless = false, args = emptyArray())

    @Synchronized
    private fun contextOrCreate(
        type: WebApplicationType,
        headless: Boolean,
        args: Array<String>,
    ): ConfigurableApplicationContext {
        appContext?.let { return it }

        if (type == WebApplicationType.NONE) {
            // Swing-based tests and manual harnesses need a real AWT
            // environment even though the production application is headless.
            System.setProperty("java.awt.headless", "false")
        }

        // Plain SpringApplication is used (instead of SpringApplicationBuilder)
        // because the builder switches to WebApplicationType.NONE when it creates
        // a child context, which would silently drop the embedded server.
        val application = SpringApplication(RocketActionSpringBootApplication::class.java).apply {
            webApplicationType = type
            setHeadless(headless)
        }

        return application.run(*args).also { appContext = it }
    }
}
