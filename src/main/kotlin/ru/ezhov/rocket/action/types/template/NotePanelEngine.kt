package ru.ezhov.rocket.action.types.template

import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.template.domain.Engine
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

class NotePanelEngine(private val originText: String, private val engine: Engine) : JPanel(BorderLayout()) {
    private val panelEngine: PanelEngine
    private val labelText: JLabel
    private fun apply() {
        val finalText = engine.apply(originText, panelEngine.apply())
        SwingUtilities.invokeLater {
            labelText.text = finalText
            val defaultToolkit = Toolkit.getDefaultToolkit()
            val clipboard = defaultToolkit.systemClipboard
            clipboard.setContents(StringSelection(finalText), null)
            NotificationFactory.notification.show(NotificationType.INFO, "Template text copy to clipboard")
        }
    }

    init {
        val words = engine.words(originText)
        panelEngine = PanelEngine(words, object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.isControlDown && e.keyCode == KeyEvent.VK_ENTER) {
                    apply()
                }
            }
        })
        add(panelEngine, BorderLayout.CENTER)
        val button = JButton("apply template (CTRL + ENTER field)")
        val panelButton = JPanel(BorderLayout())
        panelButton.add(button, BorderLayout.NORTH)
        labelText = JLabel(originText)
        panelButton.add(button, BorderLayout.NORTH)
        panelButton.add(labelText, BorderLayout.CENTER)
        add(panelButton, BorderLayout.SOUTH)
        button.addActionListener { e: ActionEvent? -> apply() }
    }
}