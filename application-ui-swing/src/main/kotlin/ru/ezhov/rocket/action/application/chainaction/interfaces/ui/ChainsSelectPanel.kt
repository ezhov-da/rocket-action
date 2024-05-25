package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainActionListCellRenderer
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingConstants

class ChainsSelectPanel(
    private val chains: List<ChainAction>,
    atomicActionService: AtomicActionService,
    private val selectChainCallback: (chain: ChainAction) -> Unit
) : JPanel(BorderLayout()) {
    private val allListChainsModel = DefaultListModel<ChainAction>()
    private val allListChains = JList(allListChainsModel)

    init {
        allListChains.cellRenderer = ChainActionListCellRenderer(atomicActionService)

        allListChains.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    allListChains.selectedValue?.let {
                        selectChainCallback(it)
                    }
                }
            }
        })

        fillList()

        val stubPanel = JPanel(BorderLayout()).apply {
            add(JLabel("Action is not used").apply {
                horizontalAlignment = SwingConstants.CENTER
                verticalAlignment = SwingConstants.CENTER
            })
        }

        if (chains.isEmpty()) {
            add(stubPanel, BorderLayout.CENTER)
        } else {
            add(JScrollPane(allListChains), BorderLayout.CENTER)
        }

        val dimension = SizeUtil.dimension(0.2, 0.2)
        size = dimension
        preferredSize = dimension
        minimumSize = dimension
        maximumSize = dimension
    }

    private fun fillList() {
        fillActions()
    }

    private fun fillActions() {
        allListChainsModel.removeAllElements()
        chains.sortedBy { it.name }.forEach {
            allListChainsModel.addElement(it)
        }
    }
}
