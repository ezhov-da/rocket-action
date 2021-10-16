package ru.ezhov.rocket.action

import com.formdev.flatlaf.FlatLightLaf
import ru.ezhov.rocket.action.configuration.infrastructure.RocketActionConfigurationRepositoryFactory
import ru.ezhov.rocket.action.infrastructure.RocketActionUiRepositoryFactory
import javax.swing.JDialog
import javax.swing.SwingUtilities

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        FlatLightLaf.setup()

        var path: String? = null
        if (args.isNotEmpty()) {
            path = args[0]
        }
        try {
            val actionService = UiQuickActionService(
                    path,
                    RocketActionConfigurationRepositoryFactory.repository,
                    RocketActionUiRepositoryFactory.repository
            )
            JDialog().apply {
                jMenuBar = actionService.createMenu(this)
                isUndecorated = true
                pack()
                setLocationRelativeTo(null)
                isAlwaysOnTop = true
                isVisible = true
            }
        } catch (e: UiQuickActionServiceException) {
            e.printStackTrace()
        }
    }
}
