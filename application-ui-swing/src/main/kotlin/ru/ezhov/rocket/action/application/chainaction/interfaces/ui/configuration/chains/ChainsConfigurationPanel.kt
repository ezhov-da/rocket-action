package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ActionListCellHeight
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.CreateAndEditChainActionDialog
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.InfoActionPopupMenuPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.LatestSearchWorker
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configureFixedRowHeight
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.replaceItems
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecorationBuilder
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecorationSnapshot
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainActionListCellRenderer
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

class ChainsConfigurationPanel(
    private val actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    private val createAndEditChainActionDialog: CreateAndEditChainActionDialog,
    private val actionSchedulerService: ActionSchedulerService,
) : JPanel(MigLayout()) {
    private val sortChainPanelConfiguration = SortChainPanelConfiguration()
    private val searchChainPanelConfiguration = SearchChainPanelConfiguration()

    private val buttonCreateChain: JButton = JButton("Create chain action")
    private val buttonEditChain: JButton = JButton("Edit").apply { isEnabled = false }
    private val buttonDuplicate: JButton = JButton("Duplicate").apply { isEnabled = false }
    private val buttonDeleteChain: JButton = JButton("Delete").apply { isEnabled = false }

    private var allListChainsModel = DefaultListModel<ChainAction>()
    private val allListChains = JList(allListChainsModel)
    private val cellRenderer = ChainActionListCellRenderer()

    private var lastAtomics: List<AtomicAction> = emptyList()
    private var lastChains: List<ChainAction> = emptyList()
    private var lastSchedulers: List<ActionScheduler> = emptyList()
    private val searchWorker =
        LatestSearchWorker<SearchResult>(onResult = { result -> applySearchResult(result) })

    init {
        allListChains.cellRenderer = cellRenderer
        allListChains.configureFixedRowHeight(ActionListCellHeight.chain())

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
                    is ChainActionCreatedDomainEvent,
                    is ChainActionDeletedDomainEvent,
                    is ChainActionUpdatedDomainEvent -> {
                        fillList()
                    }

                    is AtomicActionUpdatedDomainEvent -> {
                        // An atomic action can change the contract icon of a chain.
                        lastAtomics = lastAtomics.map {
                            if (it.id == event.atomicAction.id) event.atomicAction else it
                        }
                        applySnapshot()
                        allListChains.repaint()
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
                ChainActionCreatedDomainEvent::class.java,
                ChainActionDeletedDomainEvent::class.java,
                ChainActionUpdatedDomainEvent::class.java,

                AtomicActionUpdatedDomainEvent::class.java,

                ActionSchedulerCreatedDomainEvent::class.java,
                ActionSchedulerUpdatedDomainEvent::class.java,
                ActionSchedulerDeletedDomainEvent::class.java,
                ActionSchedulerStatusUpdatedDomainEvent::class.java,
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

        searchWorker.submit { loadChains(sortInfo, searchAction) }
    }

    private fun loadChains(sortInfo: SortInfo, searchAction: SearchAction): SearchResult {
        val chains = chainActionService.chains()
        val atomics = atomicActionService.atomics()
        val schedulers = actionSchedulerService.schedulers()

        return SearchResult(
            chains = ChainActionsFilter.filter(
                sortInfo = sortInfo,
                searchAction = searchAction,
                chains = chains,
                scheduledActionIds = schedulers.map { it.actionId }.toSet(),
            ),
            decoration = ActionRowDecorationBuilder.build(
                chains = chains,
                atomics = atomics,
                schedulers = schedulers,
            ),
            atomics = atomics,
            allChains = chains,
            schedulers = schedulers,
        )
    }

    private fun applySearchResult(result: SearchResult) {
        cellRenderer.snapshot = result.decoration
        allListChainsModel = allListChains.replaceItems(result.chains)
        lastAtomics = result.atomics
        lastChains = result.allChains
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
        allListChains.repaint()

        if (searchChainPanelConfiguration
                .searchAction()
                .conditions
                .contains(SearchAction.SearchCondition.SCHEDULER)
        ) {
            fillList()
        }
    }

    private data class SearchResult(
        val chains: List<ChainAction>,
        val decoration: ActionRowDecorationSnapshot,
        val atomics: List<AtomicAction>,
        val allChains: List<ChainAction>,
        val schedulers: List<ActionScheduler>,
    )

    private fun showPopupMenu(element: ChainAction, event: MouseEvent) {
        val popup = JPopupMenu()
        popup.add(
            JMenu("Info").apply {
                add(
                    InfoActionPopupMenuPanel(action = element)
                )
            }
        )

        element.actions.firstOrNull()
            ?.let { actionOrder -> atomicActionService.atomicBy(actionOrder.actionId) }
            ?.let { act ->

                if (act.contractType.isUnitInputContract()) {
                    popup.add(
                        ActionSchedulerJMenu(element.id, actionSchedulerService)
                    )
                }
            }

        popup.show(allListChains, event.x, event.y)
    }
}
