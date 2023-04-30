package ru.ezhov.rocket.action.application.plugin.manager.ui

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSourceType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import java.time.Duration
import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        val frame = PluginManagerFrame(
            mockk {
                every { allSpec() } returns listOf(
                    mockk<RocketActionPluginSpec.Success> {
                        every { from } returns "From info"
                        every { sourceType } returns RocketActionPluginSourceType.JAR
                        every { loadTime } returns Duration.ofMillis(100)
                    },
                    mockk<RocketActionPluginSpec.Success> {
                        every { from } returns "From info"
                        every { sourceType } returns RocketActionPluginSourceType.CLASS_PATH
                        every { loadTime } returns Duration.ofMillis(1000)
                    },
                    mockk<RocketActionPluginSpec.Failure> {
                        every { from } returns "From info"
                        every { sourceType } returns RocketActionPluginSourceType.JAR
                        every { error } returns "Error text"
                    },
                )
            }
        )

        frame.isVisible = true
    }
}

// для обнаружения тестового класса
internal class PluginManagerFrameTest
