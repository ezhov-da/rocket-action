package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel

class ResultChainDialog(
    text: String?,
) : JDialog() {
    private val resultChainPanel = ResultChainPanel(text)
    private val buttonClose = JButton("Close")

    init {
        buttonClose.addActionListener {
            this.isVisible = false
            this.dispose()
        }

        val panel = JPanel(BorderLayout())
        panel.add(resultChainPanel, BorderLayout.CENTER)
        panel.add(buttonClose, BorderLayout.SOUTH)

        add(panel, BorderLayout.CENTER)
        setSize(300, 400)
        isAlwaysOnTop = true
        isUndecorated = true
        isModal = true
    }
}
