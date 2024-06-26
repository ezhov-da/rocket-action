package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import java.awt.BorderLayout
import javax.swing.JDialog
import javax.swing.JPanel

class ResultChainDialog(
    text: String?,
) : JDialog() {
    private val resultChainPanel = ResultChainPanel(text)

    init {
        title = "Result"

        val panel = JPanel(BorderLayout())
        panel.add(resultChainPanel, BorderLayout.CENTER)

        add(panel, BorderLayout.CENTER)
        setSize(500, 400)

        defaultCloseOperation = DISPOSE_ON_CLOSE
        isAlwaysOnTop = true
        isModal = false
    }
}
