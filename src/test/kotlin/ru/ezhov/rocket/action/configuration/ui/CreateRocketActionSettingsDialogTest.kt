package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.configuration.infrastructure.ReflectionRocketActionConfigurationRepository
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository
import javax.swing.JFrame
import javax.swing.SwingUtilities

object CreateRocketActionSettingsDialogTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            val dialog = JFrame()
            dialog.setSize(1000, 900)
            val reflectionRocketActionConfigurationRepository = ReflectionRocketActionConfigurationRepository()
            val reflectionRocketActionUiRepository = ReflectionRocketActionUiRepository()
            try {
                CreateRocketActionSettingsDialog(
                    owner = dialog,
                    rocketActionConfigurationRepository = reflectionRocketActionConfigurationRepository,
                    rocketActionUiRepository = reflectionRocketActionUiRepository
                )
                    .show(object : CreatedRocketActionSettingsCallback {
                        override fun create(settings: TreeRocketActionSettings) {

                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}