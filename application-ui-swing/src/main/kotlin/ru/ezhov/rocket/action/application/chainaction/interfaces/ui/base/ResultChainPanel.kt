package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JButton
import javax.swing.JPanel

class ResultChainPanel(
    text: String?,
) : JPanel(BorderLayout()) {
    private val textArea = RSyntaxTextArea().apply {
        text?.let {
            this.text = text
        }
    }
    private val buttonCopy = JButton("Copy to clipboard")

    init {
        buttonCopy.addActionListener {
            if (!text.isNullOrBlank()) {
                val defaultToolkit = Toolkit.getDefaultToolkit()
                val clipboard = defaultToolkit.systemClipboard
                clipboard.setContents(StringSelection(text), null)
            }
        }

        add(buttonCopy, BorderLayout.NORTH)
        add(RTextScrollPane(textArea), BorderLayout.CENTER)
    }

    fun setText(text: String) {
        textArea.text = text
    }
}
