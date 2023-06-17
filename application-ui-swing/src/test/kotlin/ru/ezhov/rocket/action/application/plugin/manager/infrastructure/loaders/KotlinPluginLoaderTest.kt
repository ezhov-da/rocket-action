package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File
import javax.swing.ImageIcon
import javax.swing.JMenuItem

internal class KotlinPluginLoaderTest {
    @Test
    fun `should be success load groovy plugin`() {
        System.setProperty(UsedPropertiesName.KOTLIN_PLUGIN_FOLDER.propertyName, "../kotlin-plugins")

        val loader = KotlinPluginLoader()

        val files = loader.plugins()
        Assertions.assertThat(files[0]).isFile.hasName("copy-to-clipboard-rocket-action-ui-plugin.kt")

        val rocketAction = loader.loadPlugin(files[0])
        Assertions.assertThat(rocketAction).isInstanceOf(RocketActionPlugin::class.java)

        val action = rocketAction as RocketActionPlugin
        val component = action.factory(mockk()).create(
            settings = mockk {
                every { id() } returns "123"
                every { type() } returns mockk {
                    every { value() } returns "SIMPLE_KOTLIN_SCRIPT"
                }
                every { settings() } returns mapOf(
                    "text" to "text value",
                    "label" to "label value",
                    "description" to "description value",
                )
            },
            context = mockk {
                every { icon() } returns mockk {
                    every { by(any()) } returns ImageIcon()
                }
            }
        )
            ?.component()
        Assertions.assertThat(component).isNotNull
        Assertions.assertThat(component).isInstanceOf(JMenuItem::class.java)

        val properties = action.configuration(mockk()).properties()
        Assertions.assertThat(properties).hasSize(3)
        with(properties[0]) {
            Assertions.assertThat(this.key()).isEqualTo("label")
            Assertions.assertThat(this.name()).isEqualTo("label")
            Assertions.assertThat(this.description()).isEqualTo("Text to display")
            Assertions.assertThat(this.isRequired()).isFalse
        }
    }

    @Test
    fun `should be failure load groovy plugin when script is wrong`() {
        val loader = KotlinPluginLoader()
        val rocketAction = loader.loadPlugin(
            File.createTempFile("kotlin", "test")
                .apply {
                    writeText(text = "Test")
                    deleteOnExit()
                })
        Assertions.assertThat(rocketAction).isNull()
    }
}

