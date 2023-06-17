package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepositoryFactory
import ru.ezhov.rocket.action.application.handlers.server.model.AvailableHandler
import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu

class HandlerPanel private constructor(private val handlers: List<AvailableHandler>) : JPanel() {
    private val label = JLabel()

    init {
        add(label)
        label.apply {
            text = "Handlers available"

            addMouseListener(object : MouseAdapter() {
                val defaultColor = this@HandlerPanel.label.foreground
                val defaultMouse = this@HandlerPanel.label.cursor

                override fun mouseReleased(e: MouseEvent) {
                    val menu = JPopupMenu()
                    handlers.forEach { h ->
                        menu.add(JMenuItem().apply {
                            this.text = h.title
                            this.addActionListener { Desktop.getDesktop().browse(h.uri) }
                        })
                    }

                    menu.show(this@HandlerPanel, e.x, e.y)

                }

                override fun mouseEntered(e: MouseEvent?) {
                    this@HandlerPanel.label.foreground = Color.BLUE
                    this@HandlerPanel.label.cursor = Cursor(Cursor.HAND_CURSOR)
                }

                override fun mouseExited(e: MouseEvent?) {
                    this@HandlerPanel.label.foreground = defaultColor
                    this@HandlerPanel.label.cursor = defaultMouse
                }
            })
        }
    }

    companion object {
        fun of(rocketActionId: String): HandlerPanel? =
            AvailableHandlersRepositoryFactory
                .repository
                .by(rocketActionId)
                .takeIf { it.isNotEmpty() }
                ?.let { HandlerPanel(it) }
    }
}
