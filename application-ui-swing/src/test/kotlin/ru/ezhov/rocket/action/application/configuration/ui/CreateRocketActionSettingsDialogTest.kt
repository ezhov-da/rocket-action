package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.application.ApplicationContextFactory
import ru.ezhov.rocket.action.application.configuration.ui.create.CreateRocketActionSettingsDialog
import ru.ezhov.rocket.action.application.configuration.ui.create.CreatedRocketActionSettingsCallback
import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import javax.swing.JFrame
import javax.swing.SwingUtilities

object CreateRocketActionSettingsDialogTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val context = ApplicationContextFactory.context()
        SwingUtilities.invokeLater {
            val dialog = JFrame()
            dialog.setSize(1000, 900)

            try {
                CreateRocketActionSettingsDialog(
                    owner = dialog,
                    rocketActionPluginApplicationService = context.getBean(RocketActionPluginApplicationService::class.java),
                    rocketActionContextFactory = context.getBean(RocketActionContextFactory::class.java),
                    engineService = context.getBean(EngineService::class.java),
                    tagsService = context.getBean(TagsService::class.java),
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
