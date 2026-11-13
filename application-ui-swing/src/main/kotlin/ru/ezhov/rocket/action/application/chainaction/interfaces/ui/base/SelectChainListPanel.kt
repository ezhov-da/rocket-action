package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ActionListCellHeight
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.LatestSearchWorker
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configureFixedRowHeight
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.replaceItems
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecorationSnapshot
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainAndAtomicActionListCellRenderer
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListModel
import javax.swing.SwingUtilities

class SelectChainListPanel(
    private val chains: List<ChainAction>,
    private val atomics: List<AtomicAction>,
    private val searchTextTransformer: SearchTextTransformer,
    decorationSnapshot: ActionRowDecorationSnapshot = ActionRowDecorationSnapshot.EMPTY,
    selectedChainCallback: (Action) -> Unit,
) : JPanel(BorderLayout()) {
    private val searchTextField = TextFieldWithText("Search")
    private val clearSearchButton = JButton(Icons.Standard.X_16x16)
    private var listModel = DefaultListModel<Action>()
    private val actionsList = JList(listModel)
    private val cellRenderer = ChainAndAtomicActionListCellRenderer(decorationSnapshot)
    private val searchWorker =
        LatestSearchWorker<List<Action>>(onResult = { result -> applyResult(result) })

    init {
        actionsList.cellRenderer = cellRenderer
        actionsList.configureFixedRowHeight(ActionListCellHeight.atomic())

        fillList()

        actionsList.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1) {
                    actionsList.selectedValue?.let { selected ->
                        selectedChainCallback(selected)
                    }
                }
            }
        })

        add(JPanel(MigLayout()).apply {
            clearSearchButton.addActionListener {
                SwingUtilities.invokeLater { clearSearch() }
            }

            searchTextField.addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        SwingUtilities.invokeLater {
                            fillList(searchTextField.text.takeIf { it.isNotEmpty() })
                        }
                    }
                }
            })

            // Поддержать нажатие вниз и переход к списку из поиска
            searchTextField.addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_DOWN && !listModel.isEmpty) {
                        SwingUtilities.invokeLater {
                            actionsList.selectionModel.setSelectionInterval(0, 0)
                            actionsList.requestFocusInWindow()
                        }
                    }
                }
            })

            actionsList.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    // Поддержать нажатие вверх в списке на первом пункте и переход к поиску
                    if (e.keyCode == KeyEvent.VK_UP && actionsList.selectionModel.minSelectionIndex == 0) {
                        SwingUtilities.invokeLater {
                            searchTextField.requestFocusInWindow()
                        }
                    }
                }

                override fun keyReleased(e: KeyEvent) {
                    // Поддержать нажатие ENTER
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        actionsList.selectedValue?.let { selected ->
                            selectedChainCallback(selected)
                        }
                    }
                }
            })

            add(searchTextField, "width 100%")
            add(clearSearchButton)
        }, BorderLayout.NORTH)
        add(JScrollPane(actionsList), BorderLayout.CENTER)
    }

    private fun fillList(text: String? = null) {
        searchWorker.submit {
            if (text == null) {
                chains + atomics
            } else {
                val searchTexts = searchTextTransformer.transformedText(text)

                chains
                    .filter { ch -> searchTexts.any { st -> ch.name.lowercase().contains(st) } } +
                    atomics
                        .filter { aa -> searchTexts.any { st -> aa.name.lowercase().contains(st) } }
            }
        }
    }

    private fun applyResult(result: List<Action>) {
        listModel = actionsList.replaceItems(result)
    }

    /**
     * The clear control also restores the unfiltered list, not only the text field.
     */
    internal fun clearSearch() {
        searchTextField.text = ""
        fillList(null)
    }

    internal fun search(text: String?) {
        searchTextField.text = text.orEmpty()
        fillList(text)
    }

    internal fun currentItems(): List<Action> =
        (actionsList.model as? ListModel<Action>)?.let { model ->
            (0 until model.size).map { model.getElementAt(it) }
        }.orEmpty()

    fun selectedAction(): Action? = actionsList.selectedValue

    fun setSelectedAction(id: String) {
        listModel.elements().toList().firstOrNull { it.id() == id }?.let { action ->
            actionsList.setSelectedValue(action, true)
        }
    }

    fun activateSearchField() {
        SwingUtilities.invokeLater {
            searchTextField.requestFocusInWindow()
        }
    }
}
