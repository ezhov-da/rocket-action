package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.ActionsConfigurationPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainActionListCellRenderer
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiListener
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.eventui.model.ShowChainActionConfigurationUiEvent
import ru.ezhov.rocket.action.ui.utils.swing.common.showToFront
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.KeyStroke

class ChainConfigurationFrame(
    private val actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) : JFrame() {
    private val contentPane = JPanel(MigLayout(/*"debug"*/))

    private val actionsConfigurationPanel = ActionsConfigurationPanel(atomicActionService)

    private val buttonCreateChain: JButton = JButton("Create chain action")
    private val buttonEditChain: JButton = JButton("Edit").apply { isEnabled = false }
    private val buttonDeleteChain: JButton = JButton("Delete").apply { isEnabled = false }

    private val allListChainsModel = DefaultListModel<ChainAction>()
    private val allListChains = JList(allListChainsModel)

    private val createChainActionDialog = CreateChainActionDialog(
        actionExecutorService = actionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
    )
    private val editChainActionDialog = EditChainActionDialog(
        actionExecutorService = actionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
    )

    init {
        val chainConfigurationFrame = this
        ConfigurationUiObserverFactory.observer.register(object : ConfigurationUiListener {
            override fun action(event: ConfigurationUiEvent) {
                if (event is ShowChainActionConfigurationUiEvent) {
                    chainConfigurationFrame.showToFront(event.parent)
                }
            }
        })

        allListChains.cellRenderer = ChainActionListCellRenderer(atomicActionService)

        buttonEditChain.addActionListener {
            allListChains.selectedValue?.let {
                editChainActionDialog.showEditDialog(it, chainConfigurationFrame)
            }
        }

        buttonDeleteChain.addActionListener {
            allListChains.selectedValue?.let {
                chainActionService.deleteChain(it.id)
            }
        }

        allListChains.addListSelectionListener {
            allListChains.selectedValue?.let {
                buttonEditChain.isEnabled = true
                buttonDeleteChain.isEnabled = true
            }
        }

        val chains = chainActionService.chains()
        chains.forEach {
            allListChainsModel.addElement(it)
        }

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is ChainActionCreatedDomainEvent -> {
                        allListChainsModel.addElement(event.chainAction)
                    }

                    is ChainActionDeletedDomainEvent -> {
                        val iterator = allListChainsModel.elements()
                        while (iterator.hasMoreElements()) {
                            val action = iterator.nextElement()
                            if (action.id == event.id) {
                                allListChainsModel.removeElement(action)
                                break
                            }
                        }
                    }

                    is ChainActionUpdatedDomainEvent -> {
                        val iterator = allListChainsModel.elements()
                        while (iterator.hasMoreElements()) {
                            val action = iterator.nextElement()
                            if (action.id == event.chainAction.id) {
                                val index = allListChainsModel.indexOf(action)

                                allListChainsModel.set(index, event.chainAction)
                                break
                            }
                        }
                    }
                }
            }

            override fun subscribedToEventType(): List<Class<*>> = listOf(
                AtomicActionCreatedDomainEvent::class.java,
                AtomicActionDeletedDomainEvent::class.java,
                AtomicActionUpdatedDomainEvent::class.java,
                ChainActionCreatedDomainEvent::class.java,
                ChainActionDeletedDomainEvent::class.java,
                ChainActionUpdatedDomainEvent::class.java,
            )

        })

        val panelAtomicAction = JPanel(MigLayout())
        panelAtomicAction.add(actionsConfigurationPanel, "height max, width max")

        contentPane.add(panelAtomicAction, "width 50%")

        val panelChainAction = JPanel(MigLayout())

        panelChainAction.add(buttonCreateChain, "split 3")
        panelChainAction.add(buttonEditChain)
        panelChainAction.add(buttonDeleteChain, "wrap")
        panelChainAction.add(JScrollPane(allListChains), "height max, width max")

        contentPane.add(panelChainAction, "width 50%")

        setContentPane(contentPane)

        buttonCreateChain.apply { addActionListener { createChainActionDialog.showDialog() } }

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
            { onCancel() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        setLocationRelativeTo(null)
        setSize(1000, 800)
    }

    private fun onOK() {
        // add your code here
        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }
}
