package ru.ezhov.rocket.action.application.variables.interfaces.ui

import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.application.ApplicationContextFactory
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import javax.swing.SwingUtilities


fun main() {
    val context = ApplicationContextFactory.context()
    SwingUtilities.invokeLater {
        val frame = VariablesFrame(
            parent = null,
            variablesApplication = context.getBean(VariablesApplication::class.java),
            notificationService = context.getBean(NotificationService::class.java),
            iconService = context.getBean(IconService::class.java),
        )

        frame.isVisible = true
    }
}

// для обнаружения тестового класса
internal class VariablesFrameTest
