package ru.ezhov.rocket.action.notification

import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JWindow
import javax.swing.Timer

internal class Message(
        type: NotificationType,
        private val delay: Int,
        text: String
) : JWindow() {
    private var messageOpacity = 1f
    fun showMessage(location: Point, size: Dimension?) {
        this.size = size
        this.location = location
        val timer = Timer(20, null)
        timer.initialDelay = delay
        timer.addActionListener {
            this.messageOpacity = messageOpacity - 0.1f
            if (messageOpacity <= 0) {
                timer.stop()
                this@Message.isVisible = false
                dispose()
            } else {
                this.opacity = messageOpacity
            }
        }
        timer.start()
        this@Message.isVisible = true
    }

    init {
        val panel = JPanel(BorderLayout())
        val label = JLabel(text)
        when (type) {
            NotificationType.INFO -> label.icon = IconRepositoryFactory.repository.by(AppIcon.INFO)
            NotificationType.WARN -> label.icon = IconRepositoryFactory.repository.by(AppIcon.WARNING)
            NotificationType.ERROR -> label.icon = IconRepositoryFactory.repository.by(AppIcon.BAN)
        }
        label.horizontalAlignment = JLabel.CENTER
        panel.add(label, BorderLayout.CENTER)
        add(panel, BorderLayout.CENTER)
        isAlwaysOnTop = true
    }
}