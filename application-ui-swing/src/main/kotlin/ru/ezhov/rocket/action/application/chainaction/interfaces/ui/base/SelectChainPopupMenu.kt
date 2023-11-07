package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JPopupMenu

class SelectChainPopupMenu(
    actionService: AtomicActionService,
    chains: List<ChainAction>,
    atomics: List<AtomicAction>,
    selectedChainCallback: (Action) -> Unit
) : JPopupMenu() {
    init {
        val chainsSortedByName = chains.sortedBy { it.name }
        val atomicsSortedByName = atomics.sortedBy { it.name }

        val selectChainDialog = this

        val panel = JPanel(MigLayout(/*"debug"*/))
        panel.add(
            SelectChainButtonsPanel(
                actionService = actionService,
                chains = chainsSortedByName.take(15),
                atomics = atomicsSortedByName.take(15),
                selectedChainCallback = { sv ->
                    selectChainDialog.isVisible = false
                    selectedChainCallback(sv)
                },
            ),
            "span"
        )

        val chainsForList = chainsSortedByName.slice(15..chainsSortedByName.size)
        val atomicsForList = atomicsSortedByName.slice(15..chainsSortedByName.size)

        if (chainsForList.isNotEmpty() || atomicsForList.isNotEmpty()) {
            panel.add(
                SelectChainListPanel(
                    actionService = actionService,
                    chains = chainsForList,
                    atomics = atomicsForList,
                    selectedChainCallback = { sv ->
                        selectChainDialog.isVisible = false
                        selectedChainCallback(sv)
                    },
                ),
                "hmax 250, width 100%"
            )
        }

        add(panel, BorderLayout.CENTER)
        pack()
    }
}
