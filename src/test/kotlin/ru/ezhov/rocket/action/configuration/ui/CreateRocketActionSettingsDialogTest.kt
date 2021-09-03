package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionConfigurationRepository
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository
import javax.swing.JDialog
import javax.swing.SwingUtilities

object CreateRocketActionSettingsDialogTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            val dialog = JDialog()
            try {
                CreateRocketActionSettingsDialog(
                        dialog,
                        ReflectionRocketActionConfigurationRepository(),
                        ReflectionRocketActionUiRepository()
                )
                        .show(object : CreatedRocketActionSettingsCallback {
                            override fun create(rocketActionSettings: RocketActionSettings) {

                            }
                        })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}