package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.JXTitledSeparator
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainAndAtomicActionListCellRenderer
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel

class SelectChainButtonsPanel(
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

        add(
            InnerButtonsPanel(
                actionService = actionService,
                chains = chains,
                atomics = atomics,
                selectedChainCallback = selectedChainCallback,
            ),
            BorderLayout.CENTER
        )
    }


    private class InnerButtonsPanel(
        actionService: AtomicActionService,
        chains: List<ChainAction>,
        atomics: List<AtomicAction>,
        selectedChainCallback: (Action) -> Unit
    ) : JPanel(MigLayout("", "[fill][fill][fill]")) {
        init {
            if (chains.isNotEmpty()) {
                add(JXTitledSeparator("Chains"), "push, span 3, wrap")
            }

            chains.chunked(3).forEach { chs ->
                chs.forEachIndexed { index, ch ->
                    val button = JButton(ch.name).apply {
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseReleased(e: MouseEvent) {
                                selectedChainCallback(ch)
                            }
                        })
                    }
                    if (index == chs.size - 1) {
                        add(button, "push, wrap")
                    } else {
                        add(button, "push")
                    }
                }

            }

            if (atomics.isNotEmpty()) {
                add(JXTitledSeparator("Actions"), "push, span 3, wrap")
            }

            atomics.chunked(3).forEach { ats ->
                ats.forEachIndexed { index, at ->
                    val button = JButton(at.name).apply {
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseReleased(e: MouseEvent) {
                                selectedChainCallback(at)
                            }
                        })
                    }
                    if (index == ats.size - 1) {
                        add(button, "push, wrap")
                    } else {
                        add(button, "push")
                    }
                }
            }
        }
    }
}
