package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JPopupMenu

class SelectChainPopupMenu(
    actionService: AtomicActionService,
    chainActionService: ChainActionService,
    chains: List<ChainAction>,
    atomics: List<AtomicAction>,
    selectedChainCallback: (Action) -> Unit
) : JPopupMenu() {
    init {
        val chainsSortedByName = chains.sortedBy { it.name }
        val atomicsSortedByName = atomics.sortedBy { it.name }

        val selectChainDialog = this

        val partCount = 15

        val panel = JPanel(MigLayout(/*"debug"*/))
        panel.add(
            SelectChainButtonsPanel(
                actionService = actionService,
                chainActionService = chainActionService,
                chains = chainsSortedByName.take(partCount),
                atomics = atomicsSortedByName.take(partCount),
                selectedChainCallback = { sv ->
                    selectChainDialog.isVisible = false
                    selectedChainCallback(sv)
                },
            ),
            "span"
        )

        val chainsForList =
            chainsSortedByName.takeIf { partCount <= chainsSortedByName.size }
                ?.subList(partCount, chainsSortedByName.size).orEmpty()
        val atomicsForList =
            atomicsSortedByName.takeIf { partCount <= atomicsSortedByName.size }
                ?.subList(partCount, atomicsSortedByName.size)
                .orEmpty()

        if (chainsForList.isNotEmpty() || atomicsForList.isNotEmpty()) {
            panel.add(
                SelectChainListPanel(
                    actionService = actionService,
                    chainActionService = chainActionService,
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
