package ru.ezhov.rocket.action.notification

import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.SwingUtilities

internal class PopupNotificationService : NotificationService {
    private val messages = LinkedList<Message>()
    override fun show(type: NotificationType, text: String) {
        SwingUtilities.invokeLater { createAndShowMessage(type, text) }
    }

    private fun createAndShowMessage(type: NotificationType, text: String) {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val messageDimension = Dimension((screenSize.width * 0.1).toInt(), (screenSize.height * 0.07).toInt())
        val message = Message(type, 3000, text)
        val point = if (messages.isEmpty()) {
            Point(screenSize.width - SPACE_BETWEEN_MESSAGES - messageDimension.width,
                    screenSize.height - SPACE_BETWEEN_MESSAGES - messageDimension.height
            )
        } else {
            val messageLast = messages.last
            Point(screenSize.width - SPACE_BETWEEN_MESSAGES - messageDimension.width,
                    messageLast.y - SPACE_BETWEEN_MESSAGES - messageDimension.height
            )
        }
        message.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                messages.remove(e.source)
            }
        })
        messages.addLast(message)
        message.showMessage(point, messageDimension)
    }

    companion object {
        private const val SPACE_BETWEEN_MESSAGES = 15
    }
}