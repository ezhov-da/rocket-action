package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.ApplicationContextFactory
import ru.ezhov.rocket.action.application.CommandLineArgsSingleton
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File
import javax.swing.JLabel

internal class GroovyPluginLoaderTest {
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
    fun `should be success load groovy plugin`() {
        System.setProperty(UsedPropertiesName.GROOVY_PLUGIN_FOLDER.propertyName, "../groovy-plugins")

        val loader = ApplicationContextFactory.context().getBean(GroovyPluginLoader::class.java)

        val files = loader.plugins()
        assertThat(files[0]).isFile.hasName("simple-groovy-plugin.groovy")

        val spec = loader.loadPlugin(files[0])
        assertThat(spec).isInstanceOf(RocketActionPluginSpec.Success::class.java)

        val action = (spec as RocketActionPluginSpec.Success).rocketActionPlugin
        assertThat(action).isInstanceOf(RocketActionPlugin::class.java)
        val component = action.factory(mockk()).create(mockk(), mockk())?.component()
        assertThat(component).isNotNull
        assertThat(component).isInstanceOf(JLabel::class.java)

        val properties = action.configuration(mockk()).properties()
        assertThat(properties).hasSize(1)
        with(properties[0]) {
            assertThat(this.key()).isEqualTo("KEY")
            assertThat(this.name()).isEqualTo("KEY_NAME")
            assertThat(this.description()).isEqualTo("TEST_DESCRIPTION")
            assertThat(this.isRequired()).isTrue
        }
    }

    @Test
    fun `should be failure load groovy plugin when script is wrong`() {
        val loader = ApplicationContextFactory.context().getBean(GroovyPluginLoader::class.java)
        val spec = loader.loadPlugin(
            File.createTempFile("groovy", "test")
                .apply {
                    writeText(text = "Test")
                    deleteOnExit()
                })
        assertThat(spec).isInstanceOf(RocketActionPluginSpec.Failure::class.java)
    }
}
