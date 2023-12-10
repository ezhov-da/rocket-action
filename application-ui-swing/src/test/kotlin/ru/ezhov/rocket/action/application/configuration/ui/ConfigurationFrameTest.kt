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
                    rocketActionContextFactory = mockk(),
                    engineService = mockk(),
                    availableHandlersRepository = mockk(),
                    tagsService = mockk(),
                    generalPropertiesRepository = mockk(),
                    variablesApplication = mockk(),
                    aboutDialogFactory = mockk(),
                    httpServerService = mockk(),
                    availablePropertiesFromCommandLineDialogFactory = mockk(),
                )
                    .show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
