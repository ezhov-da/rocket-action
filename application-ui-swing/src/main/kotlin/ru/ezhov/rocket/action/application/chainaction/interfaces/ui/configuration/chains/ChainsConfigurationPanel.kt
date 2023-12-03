package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.CreateChainActionDialog
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.EditChainActionDialog
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainActionListCellRenderer
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import java.beans.PropertyChangeListener
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane

class ChainsConfigurationPanel(
    private val actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) : JPanel(MigLayout()) {
    private val sortChainPanelConfiguration = SortChainPanelConfiguration()
    private val searchChainPanelConfiguration = SearchChainPanelConfiguration()

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
        allListChains.cellRenderer = ChainActionListCellRenderer(atomicActionService)

        buttonEditChain.addActionListener {
            allListChains.selectedValue?.let {
                editChainActionDialog.showEditDialog(it, this)
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
                }
            }

            override fun subscribedToEventType(): List<Class<*>> = listOf(
                ChainActionCreatedDomainEvent::class.java,
                ChainActionDeletedDomainEvent::class.java,
                ChainActionUpdatedDomainEvent::class.java,
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

        panelChainAction.add(buttonCreateChain, "split 3")
        panelChainAction.add(buttonEditChain)
        panelChainAction.add(buttonDeleteChain, "wrap")
        panelChainAction.add(JScrollPane(allListChains), "height max, width max")

        buttonCreateChain.apply { addActionListener { createChainActionDialog.showDialog() } }

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
}
