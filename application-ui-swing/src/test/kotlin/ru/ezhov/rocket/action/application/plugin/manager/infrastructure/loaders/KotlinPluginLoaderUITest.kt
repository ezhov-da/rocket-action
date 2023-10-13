package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.ApplicationContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.awt.Dimension
import java.io.File
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.SwingUtilities

// тестирование интерфейса плагинов
fun main() {
    val context = ApplicationContextFactory.context()
    val loader = KotlinPluginLoader(
        context.getBean(VariablesApplication::class.java),
        context.getBean(GeneralPropertiesRepository::class.java),
    )
    val pluginSpec = loader.loadPlugin(File("./kotlin-plugins/copy-to-clipboard-rocket-action-ui-plugin.kt"))!!

    SwingUtilities.invokeLater {
        JFrame().apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            size = Dimension(400, 400)
            add(
                add(
                    (pluginSpec as RocketActionPluginSpec.Success).rocketActionPlugin
                        .factory(mockk()).create(
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
                                every { notification() } returns mockk {
                                    every { show(any(), any()) } returns Unit
                                }
                            }
                        )!!.component()
                )
            )
            setLocationRelativeTo(null)
            isVisible = true
        }
    }
}

