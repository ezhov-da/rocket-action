package ru.ezhov.rocket.action.application.chainaction.startup.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import java.awt.Dimension
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.ListSelectionModel

/**
 * Modal picker that lets the user choose an AtomicAction or a ChainAction for the startup list.
 */
class StartupActionPickerDialog(
    owner: Window?,
    actions: List<Action>,
) : JDialog(owner, "Add startup action", ModalityType.APPLICATION_MODAL) {
    private val filterField = JTextField()
    private val listModel = DefaultListModel<ActionChoice>()
    private val actionList = JList(listModel)
    private val okButton = JButton("OK")
    private val cancelButton = JButton("Cancel")

    var selectedActionId: String? = null
        private set

    private val allActions = actions.sortedBy { it.name().lowercase() }

    init {
        actionList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        actionList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (event.clickCount == 2) {
                    confirmSelection()
                }
            }
        })

        filterField.addCaretListener { applyFilter() }

        okButton.addActionListener { confirmSelection() }
        cancelButton.addActionListener { dispose() }

        val buttons = JPanel(MigLayout("insets 0")).apply {
            add(cancelButton, "align right, push")
            add(okButton)
        }

        val content = JPanel(MigLayout("insets 8")).apply {
            add(JLabel("Filter:"), "wrap")
            add(filterField, "width 100%, growx, wrap")
            add(JScrollPane(actionList), "width 100%, height 100%, grow, wrap")
            add(buttons, "width 100%, growx")
        }

        contentPane = content
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = Dimension(520, 420)
        setLocationRelativeTo(owner)

        applyFilter()
    }

    private fun applyFilter() {
        val text = filterField.text.trim().lowercase()
        listModel.clear()
        allActions
            .filter { text.isEmpty() || it.name().lowercase().contains(text) }
            .forEach { listModel.addElement(ActionChoice(it)) }

        if (listModel.size() > 0) {
            actionList.selectedIndex = 0
        }
    }

    private fun confirmSelection() {
        selectedActionId = actionList.selectedValue?.action?.id()
        dispose()
    }

    private data class ActionChoice(val action: Action) {
        override fun toString(): String = when (action) {
            is ChainAction -> "${action.name()} (chain)"
            is AtomicAction -> "${action.name()} (atomic)"
            else -> action.name()
        }
    }
}
