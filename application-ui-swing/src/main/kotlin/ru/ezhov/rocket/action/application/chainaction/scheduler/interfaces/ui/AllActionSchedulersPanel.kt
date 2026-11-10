package ru.ezhov.rocket.action.application.chainaction.scheduler.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.ActionSchedulerDto
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerStatusUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerUpdatedDomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.resources.Icons
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.Rectangle
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.time.format.DateTimeFormatter
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import javax.swing.Scrollable
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableRowSorter

/**
 * Shows all existing action schedulers and allows to view, edit and delete them.
 *
 * All changes are made through [ActionSchedulerService], so the corresponding domain
 * events are published and every other UI stays in sync.
 */
class AllActionSchedulersPanel(
    private val actionSchedulerService: ActionSchedulerService,
) : JPanel(MigLayout("insets 5")) {
    private val tableModel = SchedulerTableModel()
    private val table = JTable(tableModel)
    private val countLabel = JLabel()
    private val refreshButton = JButton(Icons.Standard.RELOAD_16x16).apply {
        toolTipText = "Refresh the scheduler list"
    }
    private val detailsPanel = ScrollablePanel(MigLayout("insets 0")).apply {
        border = BorderFactory.createTitledBorder("Scheduler")
        minimumSize = Dimension(0, 0)
    }

    private var currentSelectedActionId: String? = null
    private var detailActionId: String? = null
    private var reloading = false

    init {
        table.fillsViewportHeight = true
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        table.rowSorter = TableRowSorter(tableModel)

        table.selectionModel.addListSelectionListener { event ->
            if (event.valueIsAdjusting || reloading) {
                return@addListSelectionListener
            }

            val selected = selectedScheduler()
            currentSelectedActionId = selected?.action?.id()
            showDetails(selected)
        }

        refreshButton.addActionListener { reload() }

        val toolbar = JPanel(MigLayout("insets 0"))
        toolbar.add(countLabel, "push")
        toolbar.add(refreshButton)

        val tableScroll = JScrollPane(table).apply {
            minimumSize = Dimension(0, 0)
        }
        val detailsScroll = JScrollPane(detailsPanel).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            minimumSize = Dimension(0, 0)
        }

        val splitPane = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            tableScroll,
            detailsScroll,
        ).apply {
            resizeWeight = 0.55
            border = null
        }

        splitPane.addComponentListener(object : ComponentAdapter() {
            private var initialized = false

            override fun componentResized(event: ComponentEvent) {
                if (!initialized && splitPane.width > 0) {
                    initialized = true
                    splitPane.dividerLocation = (splitPane.width * 0.55).toInt()
                }
            }
        })

        add(toolbar, "width 100%, wrap")
        add(splitPane, "width 100%, height 100%, grow")

        showDetails(null)

        subscribeToDomainEvents()

        reload()
    }

    private fun subscribeToDomainEvents() {
        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is ActionSchedulerCreatedDomainEvent,
                    is ActionSchedulerUpdatedDomainEvent,
                    is ActionSchedulerDeletedDomainEvent,
                    is ActionSchedulerStatusUpdatedDomainEvent -> SwingUtilities.invokeLater { reload() }
                }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(
                    ActionSchedulerCreatedDomainEvent::class.java,
                    ActionSchedulerUpdatedDomainEvent::class.java,
                    ActionSchedulerDeletedDomainEvent::class.java,
                    ActionSchedulerStatusUpdatedDomainEvent::class.java,
                )
        })
    }

    private fun reload() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater { reload() }
            return
        }

        val selectedActionId = currentSelectedActionId

        reloading = true
        try {
            val schedulers = actionSchedulerService
                .all()
                .sortedBy { it.action.name().lowercase() }

            tableModel.setSchedulers(schedulers)
            countLabel.text = "Schedulers: ${schedulers.size}"

            val viewIndex = findViewIndex(selectedActionId)
            if (viewIndex >= 0) {
                table.selectionModel.setSelectionInterval(viewIndex, viewIndex)
                table.scrollRectToVisible(table.getCellRect(viewIndex, 0, true))
            } else {
                table.clearSelection()
                currentSelectedActionId = null
                showDetails(null)
            }
        } finally {
            reloading = false
        }
    }

    private fun findViewIndex(actionId: String?): Int {
        if (actionId == null) {
            return -1
        }

        for (viewIndex in 0 until table.rowCount) {
            val modelIndex = table.convertRowIndexToModel(viewIndex)
            if (tableModel.schedulerAt(modelIndex).action.id() == actionId) {
                return viewIndex
            }
        }

        return -1
    }

    private fun selectedScheduler(): ActionSchedulerDto? {
        val viewIndex = table.selectedRow
        if (viewIndex < 0) {
            return null
        }

        val modelIndex = table.convertRowIndexToModel(viewIndex)
        if (modelIndex < 0 || modelIndex >= tableModel.rowCount) {
            return null
        }

        return tableModel.schedulerAt(modelIndex)
    }

    private fun showDetails(scheduler: ActionSchedulerDto?) {
        if (scheduler == null) {
            detailActionId = null
            detailsPanel.removeAll()
            detailsPanel.add(JLabel("Select a scheduler to view details"), "push, align center")
            detailsPanel.revalidate()
            detailsPanel.repaint()
            return
        }

        val actionId = scheduler.action.id()
        if (detailActionId == actionId) {
            return
        }

        detailActionId = actionId

        detailsPanel.removeAll()
        detailsPanel.add(buildActionInfoPanel(scheduler), "width 100%, wrap")
        detailsPanel.add(
            ActionSchedulerPanel(
                actionId = actionId,
                actionSchedulerService = actionSchedulerService,
            ),
            "width 100%, height 100%, grow",
        )
        detailsPanel.revalidate()
        detailsPanel.repaint()
    }

    private fun buildActionInfoPanel(scheduler: ActionSchedulerDto): JPanel {
        val action = scheduler.action

        return JPanel(MigLayout("insets 0")).apply {
            border = BorderFactory.createTitledBorder("Action")

            add(JLabel(action.name()), "width 100%, wrap")
            add(JLabel("Type: ${actionTypeName(action)}"), "wrap")

            when (action) {
                is AtomicAction -> {
                    add(JLabel("Contract: ${action.contractType}"), "wrap")
                    add(JLabel("Engine: ${action.engine}"), "wrap")
                }

                is ChainAction -> {
                    add(JLabel("Atomic actions in the chain: ${action.actions.size}"), "wrap")
                }
            }

            add(JLabel("ID: ${action.id()}"), "wrap")

            action.description().takeIf { it.isNotBlank() }?.let { description ->
                val descriptionArea = JTextArea(description, 3, 40).apply {
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                    isOpaque = false
                    border = null
                }

                add(JLabel("Description:"), "wrap")
                add(JScrollPane(descriptionArea), "width 100%, hmax 100, wrap")
            }
        }
    }

    private fun actionTypeName(action: Action): String =
        when (action) {
            is ChainAction -> "Chain action"
            is AtomicAction -> "Atomic action"
            else -> action.javaClass.simpleName
        }

    private class ScrollablePanel(layout: LayoutManager) : JPanel(layout), Scrollable {
        override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

        override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int = 16

        override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int = 64

        override fun getScrollableTracksViewportWidth(): Boolean = true

        override fun getScrollableTracksViewportHeight(): Boolean = false
    }

    private class SchedulerTableModel : AbstractTableModel() {
        private val schedulers = mutableListOf<ActionSchedulerDto>()

        override fun getRowCount(): Int = schedulers.size

        override fun getColumnCount(): Int = COLUMNS.size

        override fun getColumnName(column: Int): String = COLUMNS[column]

        override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val scheduler = schedulers[rowIndex]
            return when (columnIndex) {
                0 -> scheduler.action.name()
                1 -> when (scheduler.action) {
                    is ChainAction -> "Chain"
                    is AtomicAction -> "Atomic"
                    else -> ""
                }

                2 -> scheduler.actionScheduler.cron.orEmpty()
                3 -> scheduler.actionScheduler.schedulerStatus.name
                4 -> scheduler.actionScheduler.dateOfTheLastLaunch?.format(DATE_FORMATTER) ?: "-"
                else -> ""
            }
        }

        fun schedulerAt(rowIndex: Int): ActionSchedulerDto = schedulers[rowIndex]

        fun setSchedulers(newSchedulers: List<ActionSchedulerDto>) {
            schedulers.clear()
            schedulers.addAll(newSchedulers)
            fireTableDataChanged()
        }

        companion object {
            private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            private val COLUMNS = listOf("Action", "Type", "Cron", "Status", "Last launch")
        }
    }
}
