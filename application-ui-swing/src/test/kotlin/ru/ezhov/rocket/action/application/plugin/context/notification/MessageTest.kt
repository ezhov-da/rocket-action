package ru.ezhov.rocket.action.application.plugin.context.notification

import ru.ezhov.rocket.action.api.context.notification.NotificationType
import java.awt.Dimension
import java.awt.Point
import javax.swing.SwingUtilities

object MessageTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            val message = Message(NotificationType.INFO, 4000, "Test")
            message.showMessage(Point(300, 200), Dimension(300, 200))
        }
    }
}
