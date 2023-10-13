package ru.ezhov.rocket.action.application.configuration.ui

import io.mockk.mockk
import javax.swing.SwingUtilities

object ConfigurationFrameTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            try {
                ConfigurationFrame(
                    rocketActionPluginApplicationService = mockk(),
                    rocketActionSettingsService = mockk(),
                    tagsService = mockk(),
                    rocketActionContextFactory = mockk(),
                    engineService = mockk(),
                    availableHandlersRepository = mockk(),
                    generalPropertiesRepository = mockk(),
                    variablesApplication = mockk(),
                ) { }
                    .setVisible(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
