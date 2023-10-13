package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import io.mockk.mockk
import ru.ezhov.rocket.action.application.ApplicationContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.awt.Dimension
import java.io.File
import javax.swing.JFrame
import javax.swing.SwingUtilities

// тестирование интерфейса плагинов
fun main() {
    val context = ApplicationContextFactory.context()
    val loader = GroovyPluginLoader(
        context.getBean(VariablesApplication::class.java),
        context.getBean(GeneralPropertiesRepository::class.java),
    )
    val pluginSpec = loader.loadPlugin(File("./groovy-plugins/simple-groovy-plugin.groovy"))!!

    SwingUtilities.invokeLater {
        JFrame().apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            size = Dimension(400, 400)
            add(
                (pluginSpec as RocketActionPluginSpec.Success).rocketActionPlugin.factory(mockk())
                    .create(mockk(), mockk())!!.component()
            )
            setLocationRelativeTo(null)
            isVisible = true
        }
    }
}
