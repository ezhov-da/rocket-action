package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.ApplicationContextFactory
import ru.ezhov.rocket.action.application.CommandLineArgsSingleton
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.ClassPathPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.GroovyPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.InnerPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.JarsPluginLoader
import java.io.File

class PluginsReflectionRocketActionPluginRepositoryTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            // ApplicationContextFactory is initialized statically and requires CLI args to be set.
            CommandLineArgsSingleton.args = arrayOf(
                File.createTempFile("rocket-action-test-actions", ".yml")
                    .apply {
                        delete()
                        deleteOnExit()
                    }
                    .absolutePath
            )
        }
    }

    @Test
    fun `should load plugins`() {
        val context = ApplicationContextFactory.context()
        val repo = PluginsReflectionRocketActionPluginRepository(
            innerPluginLoader = context.getBean(InnerPluginLoader::class.java),
            jarsPluginLoader = context.getBean(JarsPluginLoader::class.java),
            classPathPluginLoader = context.getBean(ClassPathPluginLoader::class.java),
            groovyPluginLoader = context.getBean(GroovyPluginLoader::class.java),
            rocketActionContextFactory = context.getBean(RocketActionContextFactory::class.java),
        )
        val list = repo.all()

        assertThat(list).isNotEmpty
    }
}
