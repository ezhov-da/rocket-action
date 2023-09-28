package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.infrastructure.yml.YmlRocketActionSettingsRepository
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationServiceFactory
import javax.swing.SwingUtilities

object ConfigurationFrameTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            try {
                ConfigurationFrame(
                    rocketActionPluginApplicationService = RocketActionPluginApplicationServiceFactory.service,
                    rocketActionSettingsService = RocketActionSettingsService(
                        RocketActionPluginApplicationServiceFactory.service,
                        YmlRocketActionSettingsRepository(
                            uri = ConfigurationFrameTest::class.java.getResource("/test-actions.yml").toURI()
                        )
                    )
                ) { }.setVisible(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
