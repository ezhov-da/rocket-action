package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationServiceFactory
import javax.swing.JFrame
import javax.swing.SwingUtilities

object CreateRocketActionSettingsDialogTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            val dialog = JFrame()
            dialog.setSize(1000, 900)
            val rocketActionPluginApplicationService = RocketActionPluginApplicationServiceFactory.service
            try {
                CreateRocketActionSettingsDialog(
                    owner = dialog,
                    rocketActionPluginApplicationService = rocketActionPluginApplicationService,
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
