package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.application.configuration.ui.create.CreateRocketActionSettingsDialog
import ru.ezhov.rocket.action.application.configuration.ui.create.CreatedRocketActionSettingsCallback
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.PluginsReflectionRocketActionPluginRepository
import javax.swing.JFrame
import javax.swing.SwingUtilities

object CreateRocketActionSettingsDialogTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            val dialog = JFrame()
            dialog.setSize(1000, 900)
            val rocketActionPluginRepository = PluginsReflectionRocketActionPluginRepository()
            try {
                CreateRocketActionSettingsDialog(
                    owner = dialog,
                    rocketActionPluginRepository = rocketActionPluginRepository,
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
