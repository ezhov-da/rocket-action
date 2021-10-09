package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.configuration.infrastructure.ReflectionRocketActionConfigurationRepository
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository
import javax.swing.JDialog
import javax.swing.SwingUtilities

object CreateRocketActionSettingsDialogTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            val dialog = JDialog()
            dialog.setSize(1000, 900)
            val reflectionRocketActionConfigurationRepository = ReflectionRocketActionConfigurationRepository()
            reflectionRocketActionConfigurationRepository.load()
            val reflectionRocketActionUiRepository = ReflectionRocketActionUiRepository()
            reflectionRocketActionUiRepository.load()
            try {
                CreateRocketActionSettingsDialog(
                        dialog,
                        reflectionRocketActionConfigurationRepository,
                        reflectionRocketActionUiRepository
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