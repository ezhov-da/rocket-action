package ru.ezhov.rocket.action.plugin.template

import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.plugin.template.domain.Engine
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities

class NotePanelEngine(
    private val originText: String,
    private val engine: Engine,
    private val context: RocketActionContext,
    ) : JPanel(BorderLayout()) {
    private val panelEngine: PanelEngine
    private val textPane: JTextPane
    private fun apply() {
        val finalText = engine.apply(originText, panelEngine.apply())
        SwingUtilities.invokeLater {
            textPane.text = finalText
            val defaultToolkit = Toolkit.getDefaultToolkit()
            val clipboard = defaultToolkit.systemClipboard
            clipboard.setContents(StringSelection(finalText), null)
            context!!.notification().show(NotificationType.INFO, "Шаблон скопирован в буфер")
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
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val panelDimension = Dimension((screenSize.width * 0.3).toInt(), (screenSize.height * 0.2).toInt())
        size = panelDimension
        minimumSize = panelDimension
        maximumSize = panelDimension
        preferredSize = panelDimension
        add(panelEngine, BorderLayout.NORTH)
        val button = JButton("Применить (CTRL + ENTER на поле)")
        val panelButton = JPanel(BorderLayout())
        panelButton.add(button, BorderLayout.NORTH)
        textPane = JTextPane().apply {
            text = originText
            isEditable = false
        }
        panelButton.add(JScrollPane(textPane), BorderLayout.CENTER)
        add(panelButton, BorderLayout.CENTER)
        button.addActionListener { apply() }
    }
}
