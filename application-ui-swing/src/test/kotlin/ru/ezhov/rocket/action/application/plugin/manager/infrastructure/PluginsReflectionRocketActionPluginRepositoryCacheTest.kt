package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSourceType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.ClassPathPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.GroovyPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.InnerPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.JarsPluginLoader
import java.time.Duration

internal class PluginsReflectionRocketActionPluginRepositoryCacheTest {
    @Test
    fun `should resolve the plugin type once for repeated lookups`() {
        val configuration = mockk<RocketActionConfiguration>()
        every { configuration.type() } returns RocketActionType { TYPE }
        val plugin = mockk<RocketActionPlugin>()
        every { plugin.configuration(any()) } returns configuration

        val spec = RocketActionPluginSpec.Success(
            rocketActionPlugin = plugin,
            from = "test",
            version = "1",
            author = "test",
            link = null,
            sourceType = RocketActionPluginSourceType.CLASS_PATH,
            loadTime = Duration.ZERO,
        )

        val inner = mockk<InnerPluginLoader>()
        every { inner.plugins() } returns listOf("test.Plugin")
        every { inner.loadPlugin(any()) } returns spec

        val jars = mockk<JarsPluginLoader>()
        every { jars.plugins() } returns emptyList()
        val classPath = mockk<ClassPathPluginLoader>()
        every { classPath.plugins() } returns emptyList()
        val groovy = mockk<GroovyPluginLoader>()
        every { groovy.plugins() } returns emptyList()

        val contextFactory = mockk<RocketActionContextFactory>()
        every { contextFactory.context } returns mockk(relaxed = true)

        val repository = PluginsReflectionRocketActionPluginRepository(
            innerPluginLoader = inner,
            jarsPluginLoader = jars,
            classPathPluginLoader = classPath,
            groovyPluginLoader = groovy,
            rocketActionContextFactory = contextFactory,
        )

        assertThat(repository.by(TYPE)).isSameAs(spec)
        assertThat(repository.by(TYPE)).isSameAs(spec)
        assertThat(repository.by("missing")).isNull()

        verify(exactly = 1) { plugin.configuration(any()) }
    }

    private companion object {
        const val TYPE = "TEST_TYPE"
    }
}
