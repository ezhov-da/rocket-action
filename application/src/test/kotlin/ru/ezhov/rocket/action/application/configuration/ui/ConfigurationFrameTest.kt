package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.application.infrastructure.YmlRocketActionSettingsRepository
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.PluginsReflectionRocketActionPluginRepository
import javax.swing.SwingUtilities

object ConfigurationFrameTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            try {
                ConfigurationFrame(
                    rocketActionPluginRepository = PluginsReflectionRocketActionPluginRepository(),
                    rocketActionSettingsRepository = YmlRocketActionSettingsRepository(
                        uri = ConfigurationFrameTest::class.java.getResource("/test-actions.yml").toURI()
                    )
                ) { }.setVisible(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
