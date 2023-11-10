package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import java.awt.BorderLayout
import javax.swing.JDialog
import javax.swing.JPanel

class ResultChainDialog(
    text: String?,
) : JDialog() {
    private val resultChainPanel = ResultChainPanel(text)

    init {
        val panel = JPanel(BorderLayout())
        panel.add(resultChainPanel, BorderLayout.CENTER)

        defaultCloseOperation = DISPOSE_ON_CLOSE

        add(panel, BorderLayout.CENTER)
        setSize(500, 400)
        isAlwaysOnTop = true
        isModal = false
    }
}
