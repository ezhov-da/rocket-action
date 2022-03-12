package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.configuration.infrastructure.ReflectionRocketActionConfigurationRepository
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository
import ru.ezhov.rocket.action.infrastructure.YmlRocketActionSettingsRepository
import javax.swing.JDialog
import javax.swing.SwingUtilities

object ConfigurationFrameTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            try {
                val dialog = JDialog()
                ConfigurationFrame(
                    owner = dialog,
                    rocketActionConfigurationRepository = ReflectionRocketActionConfigurationRepository(),
                    rocketActionUiRepository = ReflectionRocketActionUiRepository(),
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