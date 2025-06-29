package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import java.awt.BorderLayout
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.JScrollPane

class InfoActionPopupMenuPanel(
    private val action: Action,
) : JPanel(BorderLayout()) {
    private val descriptionTextField = JEditorPane().apply {
        isEditable = false
    }

    init {
        descriptionTextField.text = action.description()
        add(JScrollPane(descriptionTextField), BorderLayout.CENTER)

        val dimension = SizeUtil.dimension(0.2, 0.2)
        size = dimension
        preferredSize = dimension
        minimumSize = dimension
        maximumSize = dimension
    }
}
