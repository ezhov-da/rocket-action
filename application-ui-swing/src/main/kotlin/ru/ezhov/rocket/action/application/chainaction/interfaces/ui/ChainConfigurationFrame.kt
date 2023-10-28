package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
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
    private val chainActionExecutorService: ChainActionExecutorService,
    private val chainActionService: ChainActionService,
) : JFrame() {
    private val contentPane = JPanel(MigLayout("debug"))
    private val buttonCreateChain: JButton = JButton("Create chain action")
    private val buttonCreateAction: JButton = JButton("Create atomic action")

    private val allListActionsModel = DefaultListModel<AtomicAction>()
    private val allListActions = JList(allListActionsModel)
    private val allListChainsModel = DefaultListModel<ChainAction>()
    private val allListChains = JList(allListChainsModel)

    private val createAtomicActionDialog = CreateAtomicActionDialog(
        chainActionExecutorService = chainActionExecutorService,
        chainActionService = chainActionService
    )
    private val editAtomicActionDialog = EditAtomicActionDialog(
        chainActionExecutorService = chainActionExecutorService,
        chainActionService = chainActionService
    )
    private val createChainActionDialog = CreateChainActionDialog(
        chainActionExecutorService = chainActionExecutorService,
        chainActionService = chainActionService
    )
    private val editChainActionDialog = EditChainActionDialog(
        chainActionExecutorService = chainActionExecutorService,
        chainActionService = chainActionService
    )

    init {
        val chains = chainActionService.chains()
        chains.forEach {
            allListChainsModel.addElement(it)
        }

        val atomics = chainActionService.atomics()
        atomics.forEach {
            allListActionsModel.addElement(it)
        }

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is AtomicActionCreatedDomainEvent -> {
                        allListActionsModel.addElement(event.atomicAction)
                    }

                    is AtomicActionDeletedDomainEvent -> {
                        val iterator = allListActionsModel.elements()
                        while (iterator.hasMoreElements()) {
                            val action = iterator.nextElement()
                            if (action.id == event.id) {
                                allListActionsModel.removeElement(action)
                                break
                            }
                        }
                    }

                    is AtomicActionUpdatedDomainEvent -> {
                        val iterator = allListActionsModel.elements()
                        while (iterator.hasMoreElements()) {
                            val action = iterator.nextElement()
                            if (action.id == event.atomicAction.id) {
                                val index = allListActionsModel.indexOf(action)

                                allListActionsModel.set(index, event.atomicAction)
                                break
                            }
                        }
                    }

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
                                val index = allListActionsModel.indexOf(action)

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

        contentPane.add(buttonCreateAction, "width 50%")
        contentPane.add(buttonCreateChain, "wrap, width 50%")

        contentPane.add(JScrollPane(allListActions), "height max, width 50%")
        contentPane.add(JScrollPane(allListChains), "height max, width 50%")

        setContentPane(contentPane)

        buttonCreateChain.apply { addActionListener { createChainActionDialog.isVisible = true } }
        buttonCreateAction.apply { addActionListener { createAtomicActionDialog.isVisible = true } }

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
        setSize(700, 500)
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
