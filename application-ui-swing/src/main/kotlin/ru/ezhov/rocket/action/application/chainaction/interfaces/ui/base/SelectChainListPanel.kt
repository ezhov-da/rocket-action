package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainAndAtomicActionListCellRenderer
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

class SelectChainListPanel(
    actionService: AtomicActionService,
    chainActionService: ChainActionService,
    private val chains: List<ChainAction>,
    private val atomics: List<AtomicAction>,
    selectedChainCallback: (Action) -> Unit
) : JPanel(BorderLayout()) {
    private val searchTextField = TextFieldWithText("Search")
    private val clearSearchButton = JButton(Icons.Standard.X_16x16)
    private val listModel = DefaultListModel<Action>()
    private val actionsList = JList(listModel)

    init {
        fillList()
        actionsList.cellRenderer = ChainAndAtomicActionListCellRenderer(actionService, chainActionService)

        actionsList.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                actionsList.selectedValue?.let { selected ->
                    selectedChainCallback(selected)
                }
            }
        })

        add(JPanel(MigLayout()).apply {
            clearSearchButton.addActionListener {
                SwingUtilities.invokeLater { searchTextField.text = "" }
            }

            searchTextField.addCaretListener {
                SwingUtilities.invokeLater {
                    fillList(searchTextField.text.takeIf { it.isNotEmpty() })
                }
            }

            add(searchTextField, "width 100%")
            add(clearSearchButton)
        }, BorderLayout.NORTH)
        add(JScrollPane(actionsList), BorderLayout.CENTER)
    }

    private fun fillList(text: String? = null) {
        listModel.removeAllElements()

        if (text == null) {
            chains.forEach { listModel.addElement(it) }
            atomics.forEach { listModel.addElement(it) }
        } else {
            chains.filter { it.name.lowercase().contains(text.lowercase()) }.forEach {
                listModel.addElement(it)
            }
            atomics.filter { it.name.lowercase().contains(text.lowercase()) }.forEach {
                listModel.addElement(it)
            }
        }
    }

    fun selectedAction(): Action? = actionsList.selectedValue

    fun setSelectedAction(id: String) {
        listModel.elements().toList().firstOrNull { it.id() == id }?.let { action ->
            actionsList.setSelectedValue(action, true)
        }
    }
}
