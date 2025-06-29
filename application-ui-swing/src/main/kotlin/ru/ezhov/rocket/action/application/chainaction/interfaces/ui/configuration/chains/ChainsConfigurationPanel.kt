package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.CreateAndEditChainActionDialog
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.InfoActionPopupMenuPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainActionListCellRenderer
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.beans.PropertyChangeListener
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane

class ChainsConfigurationPanel(
    private val actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    private val createAndEditChainActionDialog: CreateAndEditChainActionDialog,
) : JPanel(MigLayout()) {
    private val sortChainPanelConfiguration = SortChainPanelConfiguration()
    private val searchChainPanelConfiguration = SearchChainPanelConfiguration()

    private val buttonCreateChain: JButton = JButton("Create chain action")
    private val buttonEditChain: JButton = JButton("Edit").apply { isEnabled = false }
    private val buttonDuplicate: JButton = JButton("Duplicate").apply { isEnabled = false }
    private val buttonDeleteChain: JButton = JButton("Delete").apply { isEnabled = false }

    private val allListChainsModel = DefaultListModel<ChainAction>()
    private val allListChains = JList(allListChainsModel)

    init {
        allListChains.cellRenderer = ChainActionListCellRenderer(atomicActionService)

        buttonEditChain.addActionListener {
            allListChains.selectedValue?.let {
                createAndEditChainActionDialog.showEditDialog(it)
            }
        }

        buttonDeleteChain.addActionListener {
            allListChains.selectedValue?.let {
                chainActionService.deleteChain(it.id)
            }
        }

        buttonDuplicate.addActionListener {
            allListChains.selectedValue?.let {
                val chain = it.duplicate()
                chainActionService.addChain(chain)
                createAndEditChainActionDialog.showEditDialog(it)
            }
        }

        allListChains.addListSelectionListener {
            allListChains.selectedValue?.let {
                buttonEditChain.isEnabled = true
                buttonDeleteChain.isEnabled = true
                buttonDuplicate.isEnabled = true
            }
        }

        allListChains.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON3) {
                    allListChainsModel.getElementAt(allListChains.locationToIndex(e.point))?.let {
                        showPopupMenu(element = it, event = e)
                    }
                }
            }
        })

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is ChainActionCreatedDomainEvent -> {
                        allListChainsModel.addElement(event.chainAction)

                        fillList()
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

                                fillList()
                                break
                            }
                        }
                    }

                    is AtomicActionUpdatedDomainEvent -> {
                        // Atomic action can change contract
                        allListChains.repaint()
                    }
                }
            }

            override fun subscribedToEventType(): List<Class<*>> = listOf(
                ChainActionCreatedDomainEvent::class.java,
                ChainActionDeletedDomainEvent::class.java,
                ChainActionUpdatedDomainEvent::class.java,

                AtomicActionUpdatedDomainEvent::class.java,
            )
        })


        val propertyChangeListener = PropertyChangeListener {
            if (
                it.propertyName == SortChainPanelConfiguration.SORT_INFO_PROPERTY_NAME ||
                it.propertyName == SearchChainPanelConfiguration.SEARCH_ACTION_PROPERTY_NAME
            ) {
                fillList()
            }
        }

        fillList()

        sortChainPanelConfiguration.addPropertyChangeListener(propertyChangeListener)
        searchChainPanelConfiguration.addPropertyChangeListener(propertyChangeListener)

        val panelChainAction = JPanel(MigLayout())

        panelChainAction.add(buttonCreateChain, "split 4")
        panelChainAction.add(buttonEditChain)
        panelChainAction.add(buttonDuplicate)
        panelChainAction.add(buttonDeleteChain, "wrap")
        panelChainAction.add(JScrollPane(allListChains), "height max, width max")

        buttonCreateChain.apply { addActionListener { createAndEditChainActionDialog.showCreateDialog() } }

        border = BorderFactory.createTitledBorder("Chains")

        add(sortChainPanelConfiguration, "wrap, width 100%")
        add(searchChainPanelConfiguration, "wrap, width 100%")
        add(panelChainAction, "width 100%")
    }

    private fun fillList() {
        val sortInfo = sortChainPanelConfiguration.sortInfo()
        val searchAction = searchChainPanelConfiguration.searchAction()

        fillActions(sortInfo, searchAction)
    }

    private fun fillActions(sortInfo: SortInfo, searchAction: SearchAction) {
        val chains = chainActionService.chains()

        val sortedChains = when (sortInfo.sortField) {
            SortField.NAME -> when (sortInfo.direction) {
                Direction.ASC -> chains.sortedBy { it.name }
                Direction.DESC -> chains.sortedByDescending { it.name }
            }

            SortField.ACTIONS_COUNT -> when (sortInfo.direction) {
                Direction.ASC -> chains.sortedBy { it.actions.size }
                Direction.DESC -> chains.sortedByDescending { it.actions.size }
            }
        }

        val filterByText = when (searchAction) {
            is SearchAction.SearchInfo -> {
                if (searchAction.text.isEmpty()) {
                    sortedChains
                } else {
                    sortedChains
                        .filter {
                            it.id.lowercase().contains(searchAction.text.lowercase()) ||
                                it.name.lowercase().contains(searchAction.text.lowercase()) ||
                                it.description.lowercase().contains(searchAction.text.lowercase())
                        }
                }
            }

            is SearchAction.Reset -> sortedChains
        }

        var filterByConditions = filterByText
//        searchAction.conditions.forEach { condition ->
//            when (condition) {
//                SearchAction.SearchCondition.IN_OUT -> filterByConditions =
//                    filterByConditions.filter { it.contractType == ContractType.IN_OUT }
//
//                SearchAction.SearchCondition.IN_UNIT -> filterByConditions =
//                    filterByConditions.filter { it.contractType == ContractType.IN_UNIT }
//
//                SearchAction.SearchCondition.UNIT_OUT -> filterByConditions =
//                    filterByConditions.filter { it.contractType == ContractType.UNIT_OUT }
//
//                SearchAction.SearchCondition.UNIT_UNIT -> filterByConditions =
//                    filterByConditions.filter { it.contractType == ContractType.UNIT_UNIT }
//
//                SearchAction.SearchCondition.KOTLIN -> filterByConditions =
//                    filterByConditions.filter { it.engine == AtomicActionEngine.KOTLIN }
//
//                SearchAction.SearchCondition.GROOVY -> filterByConditions =
//                    filterByConditions.filter { it.engine == AtomicActionEngine.GROOVY }
//
//                SearchAction.SearchCondition.TEXT -> filterByConditions =
//                    filterByConditions.filter { it.source == AtomicActionSource.TEXT }
//
//                SearchAction.SearchCondition.FILE -> filterByConditions =
//                    filterByConditions.filter { it.source == AtomicActionSource.FILE }
//            }
//        }

        allListChainsModel.removeAllElements()
        filterByConditions.forEach {
            allListChainsModel.addElement(it)
        }
    }

    private fun showPopupMenu(element: ChainAction, event: MouseEvent) {
        val popup = JPopupMenu()
        popup.add(
            JMenu("Info").apply {
                add(
                    InfoActionPopupMenuPanel(action = element)
                )
            }
        )
        popup.show(allListChains, event.x, event.y)
    }
}
