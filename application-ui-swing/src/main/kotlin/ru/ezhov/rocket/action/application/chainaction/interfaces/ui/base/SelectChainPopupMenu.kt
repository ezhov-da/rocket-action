package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionListener
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JToggleButton
import javax.swing.SwingUtilities

class SelectChainPopupMenu(
    private val configurationApplication: ConfigurationApplication,
    private val actionService: AtomicActionService,
    private val chainActionService: ChainActionService,
    private val chains: List<ChainAction>,
    private val atomics: List<AtomicAction>,
    private val selectedChainCallback: (Action) -> Unit
) : JPopupMenu() {
    init {
        val chainsSortedByName = chains.sortedBy { it.name }
        val atomicsSortedByName = atomics.sortedBy { it.name }

        val selectChainDialog = this

        val panel = JPanel(MigLayout(/*"debug"*/))

        val partCount = configurationApplication.all().numberButtonsOnChainActionSelectionPanel
        val stubUpPanel = JPanel(BorderLayout())
        val stubBottomPanel = JPanel(BorderLayout())

        panel.add(
            JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
                val acl = ActionListener { ev ->
                    val part = (ev.source as JToggleButton).text.toInt()
                    configurationApplication.all().let {
                        it.numberButtonsOnChainActionSelectionPanel = part
                        configurationApplication.save(it)
                    }

                    SwingUtilities.invokeLater {
                        stubUpPanel.removeAll()

                        stubUpPanel.add(
                            buildButtonsPanel(
                                chainsSortedByName = chainsSortedByName,
                                atomicsSortedByName = atomicsSortedByName,
                                partCount = part,
                                selectChainDialog = selectChainDialog,
                            ),
                            BorderLayout.CENTER
                        )

                        stubBottomPanel.removeAll()

                        buildListPanel(
                            chainsSortedByName = chainsSortedByName,
                            atomicsSortedByName = atomicsSortedByName,
                            partCount = part,
                            selectChainDialog = selectChainDialog,
                        )?.let {
                            stubBottomPanel.add(it, BorderLayout.CENTER)
                        }

                        selectChainDialog.repaint()
                        selectChainDialog.pack()
                    }
                }

                add(JLabel("Show buttons"))
                val b5 = JToggleButton("5").apply { addActionListener(acl) }
                val b10 = JToggleButton("10").apply { addActionListener(acl) }
                val b15 = JToggleButton("15").apply { addActionListener(acl) }
                val b20 = JToggleButton("20").apply { addActionListener(acl) }

                val bg = ButtonGroup()
                bg.add(b5)
                bg.add(b10)
                bg.add(b15)
                bg.add(b20)

                when (partCount) {
                    5 -> b5.isSelected = true
                    10 -> b10.isSelected = true
                    15 -> b15.isSelected = true
                    20 -> b10.isSelected = true
                    else -> b15.isSelected = true
                }

                add(b5)
                add(b10)
                add(b15)
                add(b20)
            },
            "width 100%, wrap"
        )

        stubUpPanel.add(
            buildButtonsPanel(
                chainsSortedByName = chainsSortedByName,
                atomicsSortedByName = atomicsSortedByName,
                partCount = partCount,
                selectChainDialog = selectChainDialog,
            )
        )
        panel.add(stubUpPanel, "width 100%, wrap")


        buildListPanel(
            chainsSortedByName = chainsSortedByName,
            atomicsSortedByName = atomicsSortedByName,
            partCount = partCount,
            selectChainDialog = selectChainDialog,
        )?.let {
            stubBottomPanel.add(
                it, BorderLayout.CENTER
            )
        }
        panel.add(stubBottomPanel, "hmax 250, width 100%")

        add(panel, BorderLayout.CENTER)
        pack()
    }

    private fun buildButtonsPanel(
        chainsSortedByName: List<ChainAction>,
        atomicsSortedByName: List<AtomicAction>,
        partCount: Int,
        selectChainDialog: SelectChainPopupMenu,
    ): SelectChainButtonsPanel =
        SelectChainButtonsPanel(
            actionService = actionService,
            chains = chainsSortedByName.take(partCount),
            atomics = atomicsSortedByName.take(partCount),
            selectedChainCallback = { sv ->
                selectChainDialog.isVisible = false
                selectedChainCallback(sv)
            },
        )

    private fun buildListPanel(
        chainsSortedByName: List<ChainAction>,
        atomicsSortedByName: List<AtomicAction>,
        partCount: Int,
        selectChainDialog: SelectChainPopupMenu,
    ): SelectChainListPanel? {
        val chainsForList =
            chainsSortedByName.takeIf { partCount <= chainsSortedByName.size }
                ?.subList(partCount, chainsSortedByName.size).orEmpty()
        val atomicsForList =
            atomicsSortedByName.takeIf { partCount <= atomicsSortedByName.size }
                ?.subList(partCount, atomicsSortedByName.size)
                .orEmpty()

        return if (chainsForList.isNotEmpty() || atomicsForList.isNotEmpty()) {
            SelectChainListPanel(
                actionService = actionService,
                chainActionService = chainActionService,
                chains = chainsForList,
                atomics = atomicsForList,
                selectedChainCallback = { sv ->
                    selectChainDialog.isVisible = false
                    selectedChainCallback(sv)
                },
            )
        } else {
            null
        }
    }
}
