package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.JXTitledSeparator
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.chainIcon
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.toIcon8x8
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class SelectChainButtonsPanel(
    actionService: AtomicActionService,
    chains: List<ChainAction>,
    atomics: List<AtomicAction>,
    selectedChainCallback: (Action) -> Unit
) : JPanel(BorderLayout()) {

    init {
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
    ) : JPanel(MigLayout("", "[fill][fill]")) {
        init {
            val chainButtons = chains.map { ch ->

                JButton(ch.name).apply {
                    icon = chainIcon(chain = ch, atomicActionService = actionService)
                    horizontalAlignment = SwingConstants.LEFT
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent) {
                            selectedChainCallback(ch)
                        }
                    })
                }
            }

            add(ButtonsPanel("Chains", chainButtons))

            val atomicButtons = atomics.map { at ->
                JButton(at.name).apply {
                    icon = at.contractType.toIcon8x8()
                    horizontalAlignment = SwingConstants.LEFT
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent) {
                            selectedChainCallback(at)
                        }
                    })
                }
            }

            add(ButtonsPanel("Actions", atomicButtons))
        }
    }

    private class ButtonsPanel(title: String, components: List<Component>) : JPanel(MigLayout("", "[fill]")) {
        init {
            add(JXTitledSeparator(title), "push, wrap")
            components.forEach {
                add(it, "push, wrap")
            }
        }
    }
}
