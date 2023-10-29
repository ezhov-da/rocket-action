package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel

class ResultChainDialog(
    text: String?,
) : JDialog() {
    private val textArea = RSyntaxTextArea().apply {
        text?.let {
            this.text = text
        }
    }
    private val buttonClose = JButton("Close")

    init {
        buttonClose.addActionListener {
            this.isVisible = false
            this.dispose()
        }

        val panel = JPanel(BorderLayout())
        panel.add(RTextScrollPane(textArea), BorderLayout.CENTER)
        panel.add(buttonClose, BorderLayout.SOUTH)

        add(panel, BorderLayout.CENTER)
        setSize(300, 400)
        isAlwaysOnTop = true
        isUndecorated = true
        isModal = true
    }
}
