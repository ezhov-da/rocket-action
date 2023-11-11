package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.CreateAndEditAtomicActionDialog
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.AtomicActionListCellRenderer
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import java.beans.PropertyChangeListener
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane

class ActionsConfigurationPanel(
    private val atomicActionService: AtomicActionService,
) : JPanel(MigLayout()) {
    private val sortActionPanelConfiguration = SortActionPanelConfiguration()
    private val searchActionPanelConfiguration = SearchActionPanelConfiguration()

    private val buttonCreateAction: JButton = JButton("Create atomic action")
    private val buttonEditAction: JButton = JButton("Edit").apply { isEnabled = false }
    private val buttonDeleteAction: JButton = JButton("Delete").apply { isEnabled = false }

    private val allListActionsModel = DefaultListModel<AtomicAction>()
    private val allListActions = JList(allListActionsModel)

    private val createAndEditAtomicActionDialog = CreateAndEditAtomicActionDialog(
        atomicActionService = atomicActionService
    )

    init {
        allListActions.cellRenderer = AtomicActionListCellRenderer()

        allListActions.addListSelectionListener {
            allListActions.selectedValue?.let {
                buttonEditAction.isEnabled = true
                buttonDeleteAction.isEnabled = true
            }
        }

        buttonEditAction.addActionListener {
            allListActions.selectedValue?.let {
                createAndEditAtomicActionDialog.showEditDialog(it, this)
            }
        }

        buttonDeleteAction.addActionListener {
            allListActions.selectedValue?.let {
                atomicActionService.deleteAtomic(it.id)
            }
        }

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is AtomicActionCreatedDomainEvent -> {
                        fillList()
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

                        fillList()
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

                        fillList()
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


        val propertyChangeListener = PropertyChangeListener {
            if (
                it.propertyName == SortActionPanelConfiguration.SORT_INFO_PROPERTY_NAME ||
                it.propertyName == SearchActionPanelConfiguration.SEARCH_ACTION_PROPERTY_NAME
            ) {
                fillList()
            }
        }

        fillList()

        sortActionPanelConfiguration.addPropertyChangeListener(propertyChangeListener)
        searchActionPanelConfiguration.addPropertyChangeListener(propertyChangeListener)

        val panelAtomicAction = JPanel(MigLayout())
        panelAtomicAction.add(buttonCreateAction, "split 3")
        panelAtomicAction.add(buttonEditAction)
        panelAtomicAction.add(buttonDeleteAction, "wrap")
        panelAtomicAction.add(JScrollPane(allListActions), "height max, width max")

        buttonCreateAction.apply { addActionListener { createAndEditAtomicActionDialog.showCreateDialog() } }

        add(sortActionPanelConfiguration, "wrap, width 100%")
        add(searchActionPanelConfiguration, "wrap, width 100%")
        add(panelAtomicAction, "width 100%")
    }

    private fun fillList() {
        val sortInfo = sortActionPanelConfiguration.sortInfo()
        val searchAction = searchActionPanelConfiguration.searchAction()

        fillActions(sortInfo, searchAction)
    }

    private fun fillActions(sortInfo: SortInfo, searchAction: SearchAction) {
        val atomics = atomicActionService.atomics()

        val sortedAtomics = when (sortInfo.sortField) {
            SortField.NAME -> when (sortInfo.direction) {
                Direction.ASC -> atomics.sortedBy { it.name }
                Direction.DESC -> atomics.sortedByDescending { it.name }
            }

            SortField.ENGINE -> when (sortInfo.direction) {
                Direction.ASC -> atomics.sortedBy { it.engine }
                Direction.DESC -> atomics.sortedByDescending { it.engine }
            }

            SortField.CONTRACT -> when (sortInfo.direction) {
                Direction.ASC -> atomics.sortedBy { it.contractType }
                Direction.DESC -> atomics.sortedByDescending { it.contractType }
            }

            SortField.SOURCE -> when (sortInfo.direction) {
                Direction.ASC -> atomics.sortedBy { it.source }
                Direction.DESC -> atomics.sortedByDescending { it.source }
            }
        }

        val filterByText = when (searchAction) {
            is SearchAction.SearchInfo -> {
                if (searchAction.text.isEmpty()) {
                    sortedAtomics
                } else {
                    sortedAtomics
                        .filter {
                            it.name.lowercase().contains(searchAction.text.lowercase()) ||
                                it.description.lowercase().contains(searchAction.text.lowercase()) ||
                                it.data.lowercase().contains(searchAction.text.lowercase())
                        }
                }
            }

            is SearchAction.Reset -> sortedAtomics
        }

        var filterByConditions = filterByText
        searchAction.conditions.forEach { condition ->
            when (condition) {
                SearchAction.SearchCondition.IN_OUT -> filterByConditions =
                    filterByConditions.filter { it.contractType == ContractType.IN_OUT }

                SearchAction.SearchCondition.IN_UNIT -> filterByConditions =
                    filterByConditions.filter { it.contractType == ContractType.IN_UNIT }

                SearchAction.SearchCondition.UNIT_OUT -> filterByConditions =
                    filterByConditions.filter { it.contractType == ContractType.UNIT_OUT }

                SearchAction.SearchCondition.UNIT_UNIT -> filterByConditions =
                    filterByConditions.filter { it.contractType == ContractType.UNIT_UNIT }

                SearchAction.SearchCondition.KOTLIN -> filterByConditions =
                    filterByConditions.filter { it.engine == AtomicActionEngine.KOTLIN }

                SearchAction.SearchCondition.GROOVY -> filterByConditions =
                    filterByConditions.filter { it.engine == AtomicActionEngine.GROOVY }

                SearchAction.SearchCondition.TEXT -> filterByConditions =
                    filterByConditions.filter { it.source == AtomicActionSource.TEXT }

                SearchAction.SearchCondition.FILE -> filterByConditions =
                    filterByConditions.filter { it.source == AtomicActionSource.FILE }
            }
        }

        allListActionsModel.removeAllElements()
        filterByConditions.forEach {
            allListActionsModel.addElement(it)
        }
    }
}
