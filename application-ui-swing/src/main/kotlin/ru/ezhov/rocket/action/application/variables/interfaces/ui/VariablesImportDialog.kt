package ru.ezhov.rocket.action.application.variables.interfaces.ui

import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.infrastructure.importv.PlainTextImportVariablesService
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

class VariablesImportDialog(
    owner: Frame,
    private val variables: (List<Variable>) -> Unit
) {
    private val dialog: JDialog = JDialog(owner, "Import variables")
    private val label = JLabel("Input text as `VARIABLE_NAME=VARIABLE_VALUE` in one line")
    private val text = JEditorPane()
    private val applyButton = JButton("Import")

    init {
        applyButton.addActionListener {
            if (text.text.isNotBlank()) {
                SwingUtilities.invokeLater {
                    val variablesParse = PlainTextImportVariablesService(text.text).variables()
                    variables(variablesParse)
                    dialog.isVisible = false
                }
            }
        }

        dialog.add(label, BorderLayout.NORTH)
        dialog.add(JScrollPane(text), BorderLayout.CENTER)
        dialog.add(JScrollPane(applyButton), BorderLayout.SOUTH)
        val dimension = SizeUtil.dimension(0.4, 0.4)
        dialog.size = dimension
        dialog.preferredSize = dimension
        dialog.minimumSize = dimension
        dialog.maximumSize = dimension
        dialog.setLocationRelativeTo(owner)
    }

    fun showDialog() {
        text.text = ""
        dialog.isVisible = true
    }
}
