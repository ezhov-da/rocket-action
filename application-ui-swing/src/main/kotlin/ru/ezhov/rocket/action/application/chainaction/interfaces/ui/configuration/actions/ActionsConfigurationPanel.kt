package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

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
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ActionListCellHeight
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainsSelectPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.CreateAndEditAtomicActionDialog
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.CreateAndEditChainActionDialog
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.InfoActionPopupMenuPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.LatestSearchWorker
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configureFixedRowHeight
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.replaceItems
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecorationBuilder
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecorationSnapshot
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.AtomicActionListCellRenderer
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerStatusUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.interfaces.ui.ActionSchedulerJMenu
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
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
import javax.swing.SwingUtilities

class ActionsConfigurationPanel(
    private val atomicActionService: AtomicActionService,
    private val chainActionService: ChainActionService,
    private val createAndEditChainActionDialog: CreateAndEditChainActionDialog,
    private val actionSchedulerService: ActionSchedulerService,
    actionExecutorService: ActionExecutorService,
    iconRepository: IconRepository,
) : JPanel(MigLayout()) {
    private val sortActionPanelConfiguration = SortActionPanelConfiguration()
    private val searchActionPanelConfiguration = SearchActionPanelConfiguration()

    private val buttonCreateAction: JButton = JButton("Create atomic action")
    private val buttonCreateChainFromAction: JButton =
        JButton("Create chain from atomic action").apply { isEnabled = false }
    private val buttonDuplicate: JButton = JButton("Duplicate").apply { isEnabled = false }
    private val buttonEditAction: JButton = JButton("Edit").apply { isEnabled = false }
    private val buttonDeleteAction: JButton = JButton("Delete").apply { isEnabled = false }

    private var allListActionsModel = DefaultListModel<AtomicAction>()
    private val allListActions = JList(allListActionsModel)
    private val cellRenderer = AtomicActionListCellRenderer()

    private var lastAtomics: List<AtomicAction> = emptyList()
    private var lastChains: List<ChainAction> = emptyList()
    private var lastSchedulers: List<ActionScheduler> = emptyList()
    private val searchWorker =
        LatestSearchWorker<SearchResult>(onResult = { result -> applySearchResult(result) })

    private val createAndEditAtomicActionDialog = CreateAndEditAtomicActionDialog(
        atomicActionService = atomicActionService,
        chainActionService = chainActionService,
        actionExecutorService = actionExecutorService,
        iconRepository = iconRepository,
    )

    init {
        allListActions.cellRenderer = cellRenderer
        allListActions.configureFixedRowHeight(ActionListCellHeight.atomic())

        allListActions.addListSelectionListener {
            allListActions.selectedValue?.let {
                buttonEditAction.isEnabled = true
                buttonDeleteAction.isEnabled = true
                buttonCreateChainFromAction.isEnabled = true
                buttonDuplicate.isEnabled = true
            }
        }

        allListActions.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON3) {
                    allListActionsModel.getElementAt(allListActions.locationToIndex(e.point))?.let {
                        showPopupMenu(element = it, event = e)
                    }
                }
            }
        })

        buttonEditAction.addActionListener {
            allListActions.selectedValue?.let {
                createAndEditAtomicActionDialog.showEditDialog(it)
            }
        }

        buttonDeleteAction.addActionListener {
            allListActions.selectedValue?.let {
                atomicActionService.deleteAtomic(it.id)
            }
        }

        buttonCreateChainFromAction.addActionListener {
            allListActions.selectedValue?.let {
                createAndEditChainActionDialog.showCreateDialogWith(it)
            }
        }

        buttonDuplicate.addActionListener {
            allListActions.selectedValue?.let {
                val action = it.duplicate()
                atomicActionService.addAtomic(action)
                createAndEditAtomicActionDialog.showEditDialog(action)
            }
        }

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is AtomicActionCreatedDomainEvent -> {
                        fillList()
                    }

                    is AtomicActionDeletedDomainEvent -> {
                        lastAtomics = lastAtomics.filterNot { it.id == event.id }
                        fillList()
                    }

                    is AtomicActionUpdatedDomainEvent -> {
                        val index = allListActionsModel.elements().toList().indexOfFirst { it.id == event.atomicAction.id }
                        if (index >= 0) {
                            allListActionsModel.set(index, event.atomicAction)
                        }

                        lastAtomics = lastAtomics.map {
                            if (it.id == event.atomicAction.id) event.atomicAction else it
                        }
                        applySnapshot()
                        fillList()
                    }

                    is ChainActionCreatedDomainEvent -> {
                        lastChains = lastChains.filterNot { it.id == event.chainAction.id } + event.chainAction
                        applySnapshot()
                        allListActions.repaint()
                    }

                    is ChainActionUpdatedDomainEvent -> {
                        lastChains = lastChains.map {
                            if (it.id == event.chainAction.id) event.chainAction else it
                        }
                        applySnapshot()
                        allListActions.repaint()
                    }

                    is ChainActionDeletedDomainEvent -> {
                        lastChains = lastChains.filterNot { it.id == event.id }
                        applySnapshot()
                        allListActions.repaint()
                    }

                    is ActionSchedulerCreatedDomainEvent,
                    is ActionSchedulerUpdatedDomainEvent,
                    is ActionSchedulerDeletedDomainEvent,
                    is ActionSchedulerStatusUpdatedDomainEvent -> {
                        SwingUtilities.invokeLater { refreshSchedulerDependentState() }
                    }
                }
            }

            override fun subscribedToEventType(): List<Class<*>> = listOf(
                AtomicActionCreatedDomainEvent::class.java,
                AtomicActionDeletedDomainEvent::class.java,
                AtomicActionUpdatedDomainEvent::class.java,

                ChainActionCreatedDomainEvent::class.java,
                ChainActionUpdatedDomainEvent::class.java,
                ChainActionDeletedDomainEvent::class.java,

                ActionSchedulerCreatedDomainEvent::class.java,
                ActionSchedulerUpdatedDomainEvent::class.java,
                ActionSchedulerDeletedDomainEvent::class.java,
                ActionSchedulerStatusUpdatedDomainEvent::class.java,
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
        panelAtomicAction.add(buttonCreateAction, "split 5")
        panelAtomicAction.add(buttonCreateChainFromAction)
        panelAtomicAction.add(buttonEditAction)
        panelAtomicAction.add(buttonDuplicate)
        panelAtomicAction.add(buttonDeleteAction, "wrap")
        panelAtomicAction.add(JScrollPane(allListActions), "height max, width max")

        buttonCreateAction.apply { addActionListener { createAndEditAtomicActionDialog.showCreateDialog() } }

        border = BorderFactory.createTitledBorder("Actions")

        add(sortActionPanelConfiguration, "wrap, width 100%")
        add(searchActionPanelConfiguration, "wrap, width 100%")
        add(panelAtomicAction, "width 100%")
    }

    private fun fillList() {
        val sortInfo = sortActionPanelConfiguration.sortInfo()
        val searchAction = searchActionPanelConfiguration.searchAction()

        searchWorker.submit {
            val atomics = atomicActionService.atomics()
            val chains = chainActionService.chains()
            val schedulers = actionSchedulerService.schedulers()
            val filtered = AtomicActionsFilter.filter(
                sortInfo = sortInfo,
                searchAction = searchAction,
                actions = atomics,
                scheduledActionIds = schedulers.map { it.actionId }.toSet(),
            )
            val snapshot = ActionRowDecorationBuilder.build(
                chains = chains,
                atomics = atomics,
                schedulers = schedulers,
            )

            SearchResult(
                actions = filtered,
                decoration = snapshot,
                atomics = atomics,
                chains = chains,
                schedulers = schedulers,
            )
        }
    }

    private fun applySearchResult(result: SearchResult) {
        cellRenderer.snapshot = result.decoration
        allListActionsModel = allListActions.replaceItems(result.actions)
        lastAtomics = result.atomics
        lastChains = result.chains
        lastSchedulers = result.schedulers
    }

    private fun applySnapshot() {
        cellRenderer.snapshot = ActionRowDecorationBuilder.build(
            chains = lastChains,
            atomics = lastAtomics,
            schedulers = lastSchedulers,
        )
    }

    /**
     * The scheduler state affects the rendered scheduler status and the "used in scheduler" filter.
     */
    private fun refreshSchedulerDependentState() {
        lastSchedulers = actionSchedulerService.schedulers()
        applySnapshot()
        allListActions.repaint()

        if (searchActionPanelConfiguration
                .searchAction()
                .conditions
                .contains(SearchAction.SearchCondition.SCHEDULER)
        ) {
            fillList()
        }
    }

    private data class SearchResult(
        val actions: List<AtomicAction>,
        val decoration: ActionRowDecorationSnapshot,
        val atomics: List<AtomicAction>,
        val chains: List<ChainAction>,
        val schedulers: List<ActionScheduler>,
    )


    private fun showPopupMenu(element: AtomicAction, event: MouseEvent) {
        val chains = chainActionService.usageAction(element.id)

        val popup = JPopupMenu()
        popup.add(
            JMenu("Info").apply {
                add(
                    InfoActionPopupMenuPanel(action = element)
                )
            }
        )
        popup.add(
            JMenu("Usage").apply {
                add(
                    ChainsSelectPanel(
                        chains = chains,
                        decorationSnapshot = cellRenderer.snapshot,
                        selectChainCallback = { chain -> createAndEditChainActionDialog.showEditDialog(chain) }
                    )
                )
            }
        )

        if (element.contractType.isUnitInputContract()) {
            popup.add(
                ActionSchedulerJMenu(element.id, actionSchedulerService)
            )
        }

        popup.show(allListActions, event.x, event.y)
    }
}
