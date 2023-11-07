package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainAndAtomicActionListCellRenderer
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane

class SelectChainListPanel(
    actionService: AtomicActionService,
    chains: List<ChainAction>,
    atomics: List<AtomicAction>,
    selectedChainCallback: (Action) -> Unit
) : JPanel(BorderLayout()) {
    private val listChainsModel = DefaultListModel<Action>()
    private val chainList = JList(listChainsModel)

    init {
        chains.forEach { listChainsModel.addElement(it) }
        atomics.forEach { listChainsModel.addElement(it) }
        chainList.cellRenderer = ChainAndAtomicActionListCellRenderer(actionService)

        chainList.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                chainList.selectedValue?.let { selected ->
                    selectedChainCallback(selected)
                }
            }
        })

        add(JScrollPane(chainList), BorderLayout.CENTER)
    }
}
