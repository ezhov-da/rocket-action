package ru.ezhov.rocket.action.application.chainaction.startup.interfaces.ui

import mu.KotlinLogging
import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.startup.application.StartupActionRunner
import ru.ezhov.rocket.action.application.chainaction.startup.application.StartupActionService
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionLogRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionEntryFinishedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionEntryStartedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionRunFinishedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionRunStartedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionsUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import java.awt.Dimension
import java.awt.Desktop
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.AbstractTableModel

private val logger = KotlinLogging.logger {}

/**
 * Editor of the ordered startup list with the result of the last run.
 *
 * While a run is active the editing controls are disabled, but the list, the details and the log
 * stay readable. The rest of the application is not blocked.
 */
class StartupActionsPanel(
    startupActionService: StartupActionService,
    private val runner: StartupActionRunner,
    private val logRepository: StartupActionLogRepository,
) : JPanel(MigLayout("insets 5")) {
    private val editor = StartupActionsEditor(startupActionService) { runner.state() }
    private val tableModel = StartupActionTableModel(editor)
    private val table = JTable(tableModel)

    private val addButton = JButton("Add")
    private val removeButton = JButton("Remove")
    private val upButton = JButton("Up")
    private val downButton = JButton("Down")
    private val saveButton = JButton("Save")
    private val runNowButton = JButton("Run now")
    private val stopOnErrorCheck = JCheckBox("Stop on first error")
    private val progressLabel = JLabel("Last run: not started")
    private val dirtyLabel = JLabel("")

    private val nameLabel = JLabel("-")
    private val idLabel = JLabel("-")
    private val typeLabel = JLabel("-")
    private val inputHintLabel = JLabel(" ")
    private val inputField = JTextField()
    private val enabledCheck = JCheckBox("Enabled")
    private val errorArea = JTextArea(3, 40).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }

    private var updating = false
    private var updatingDetails = false

    init {
        table.fillsViewportHeight = true
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        table.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN

        table.selectionModel.addListSelectionListener { event ->
            if (event.valueIsAdjusting || updating) {
                return@addListSelectionListener
            }

            editor.select(table.selectedRow)
            updateDetails()
            updateControls()
        }

        addButton.addActionListener { addEntry() }
        removeButton.addActionListener {
            editor.removeSelected()
            refresh()
        }
        upButton.addActionListener {
            editor.moveUp()
            refresh()
        }
        downButton.addActionListener {
            editor.moveDown()
            refresh()
        }
        saveButton.addActionListener { save() }
        runNowButton.addActionListener { runNow() }

        stopOnErrorCheck.addActionListener {
            if (updatingDetails) {
                return@addActionListener
            }
            editor.setStopOnError(stopOnErrorCheck.isSelected)
            updateDirty()
        }

        enabledCheck.addActionListener {
            if (updatingDetails) {
                return@addActionListener
            }
            editor.setEnabled(enabledCheck.isSelected)
            refresh()
        }

        inputField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent) = inputChanged()

            override fun removeUpdate(event: DocumentEvent) = inputChanged()

            override fun changedUpdate(event: DocumentEvent) = inputChanged()
        })

        val toolbar = buildToolbar()
        val splitPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            JScrollPane(table),
            buildDetailsPanel(),
        ).apply {
            resizeWeight = 0.65
            border = null
        }

        add(toolbar, "width 100%, wrap")
        add(splitPane, "width 100%, height 100%, grow")

        subscribeToDomainEvents()

        editor.reload()
        refresh()
    }

    private fun buildToolbar(): JPanel =
        JPanel(MigLayout("insets 0")).apply {
            add(addButton)
            add(removeButton)
            add(upButton)
            add(downButton)
            add(saveButton)
            add(runNowButton)
            add(stopOnErrorCheck, "gapleft 15")
            add(dirtyLabel, "gapleft 15")
            add(progressLabel, "push, align right")
        }

    private fun buildDetailsPanel(): JPanel =
        JPanel(MigLayout("insets 5")).apply {
            border = javax.swing.BorderFactory.createTitledBorder("Details")

            add(JLabel("Name:"))
            add(nameLabel, "growx, pushx, wrap")

            add(JLabel("ID:"))
            add(idLabel, "growx, pushx, wrap")

            add(JLabel("Type:"))
            add(typeLabel, "growx, pushx, wrap")

            add(enabledCheck, "span 2, wrap")

            add(JLabel("Input:"))
            add(inputField, "growx, pushx, wrap")

            add(inputHintLabel, "span 2, wrap")

            add(JLabel("Last error:"), "top")
            add(
                JScrollPane(errorArea).apply {
                    horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                    minimumSize = Dimension(0, 0)
                },
                "grow, push, span 2, hmax 120, wrap",
            )

            val openLogButton = JButton("Open log")
            openLogButton.addActionListener { openLog() }
            add(openLogButton, "span 2, align left")
        }

    private fun subscribeToDomainEvents() {
        val subscriber = object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is StartupActionsUpdatedDomainEvent -> SwingUtilities.invokeLater {
                        if (!editor.dirty) {
                            editor.reload()
                        }
                        refresh()
                    }

                    is StartupActionRunStartedDomainEvent,
                    is StartupActionEntryStartedDomainEvent,
                    is StartupActionEntryFinishedDomainEvent,
                    is StartupActionRunFinishedDomainEvent -> SwingUtilities.invokeLater { refreshRunState() }
                }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(
                    StartupActionsUpdatedDomainEvent::class.java,
                    StartupActionRunStartedDomainEvent::class.java,
                    StartupActionEntryStartedDomainEvent::class.java,
                    StartupActionEntryFinishedDomainEvent::class.java,
                    StartupActionRunFinishedDomainEvent::class.java,
                )
        }

        DomainEventFactory.subscriberRegistrar.subscribe(subscriber)

        addAncestorListener(object : AncestorListener {
            override fun ancestorAdded(event: AncestorEvent) {
            }

            override fun ancestorRemoved(event: AncestorEvent) {
                DomainEventFactory.subscriberRegistrar.unsubscribe(subscriber)
            }

            override fun ancestorMoved(event: AncestorEvent) {
            }
        })
    }

    private fun refreshRunState() {
        editor.refreshRunState()
        refresh()
    }

    private fun refresh() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater { refresh() }
            return
        }

        updating = true
        try {
            tableModel.fireTableDataChanged()

            if (editor.entries.isEmpty()) {
                table.clearSelection()
                editor.select(-1)
            } else {
                val index = editor.selection.coerceIn(0, editor.entries.lastIndex)
                editor.select(index)
                table.selectionModel.setSelectionInterval(index, index)
                table.scrollRectToVisible(table.getCellRect(index, 0, true))
            }

            updateDetails()
            updateControls()
        } finally {
            updating = false
        }
    }

    private fun updateDetails() {
        updatingDetails = true
        try {
            val entry = editor.selectedEntry()
            stopOnErrorCheck.isSelected = editor.stopOnError

            if (entry == null) {
                nameLabel.text = "-"
                idLabel.text = "-"
                typeLabel.text = "-"
                inputField.text = ""
                inputField.isEnabled = false
                inputHintLabel.text = " "
                enabledCheck.isSelected = false
                enabledCheck.isEnabled = false
                errorArea.text = ""
                return
            }

            val resolved = editor.isResolved(entry)
            nameLabel.text = editor.actionName(entry.actionId)
            idLabel.text = entry.id
            typeLabel.text = if (resolved) editor.actionType(entry.actionId) else "? (action not found)"
            enabledCheck.isSelected = entry.enabled

            val acceptsInput = editor.acceptsInput(entry)
            inputField.text = entry.input.orEmpty()
            inputField.isEnabled = editor.editable && acceptsInput
            inputHintLabel.text = if (acceptsInput) {
                "The value is passed to the action when the list runs."
            } else {
                "This action does not accept input; the value is ignored."
            }
            errorArea.text = editor.errorOf(entry.id).orEmpty()
        } finally {
            updatingDetails = false
        }
    }

    private fun updateControls() {
        val editable = editor.editable

        addButton.isEnabled = editable
        removeButton.isEnabled = editable
        upButton.isEnabled = editable
        downButton.isEnabled = editable
        saveButton.isEnabled = editable
        stopOnErrorCheck.isEnabled = editable
        enabledCheck.isEnabled = editable && editor.selectedEntry() != null
        inputField.isEnabled = editable &&
            editor.selectedEntry()?.let { editor.acceptsInput(it) } == true
        runNowButton.isEnabled = editor.canRunNow

        progressLabel.text = editor.progressText() ?: "Last run: not started"
        updateDirty()
    }

    private fun updateDirty() {
        dirtyLabel.text = if (editor.dirty) "Unsaved changes" else ""
        runNowButton.isEnabled = editor.canRunNow
    }

    private fun addEntry() {
        val actions = editor.availableActions()
        if (actions.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "There are no atomic or chain actions to add.",
                "Add startup action",
                JOptionPane.INFORMATION_MESSAGE,
            )
            return
        }

        val dialog = StartupActionPickerDialog(SwingUtilities.getWindowAncestor(this), actions)
        dialog.isVisible = true
        dialog.selectedActionId?.let { actionId ->
            editor.addAction(actionId)
            refresh()
        }
    }

    private fun save() {
        try {
            editor.save()
            refresh()
        } catch (ex: Exception) {
            logger.error(ex) { "Can not save startup actions" }
            showError("Can not save the startup list.\n\nReason: ${ex.message ?: ex.javaClass.simpleName}")
        }
    }

    private fun runNow() {
        if (!editor.canRunNow) {
            return
        }

        if (editor.dirty) {
            val choice = JOptionPane.showConfirmDialog(
                this,
                "The startup list has unsaved changes.\nSave the list and run it?",
                "Run now",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
            )
            if (choice != JOptionPane.YES_OPTION) {
                return
            }

            try {
                editor.save()
            } catch (ex: Exception) {
                logger.error(ex) { "Can not save startup actions before a manual run" }
                showError("Can not save the startup list.\n\nReason: ${ex.message ?: ex.javaClass.simpleName}")
                return
            }
        }

        refresh()
        runner.startAsync(StartupRunTrigger.MANUAL)
        refreshRunState()
    }

    private fun inputChanged() {
        if (updatingDetails) {
            return
        }

        editor.setInput(inputField.text)
        tableModel.fireTableDataChanged()
        updateDirty()
    }

    private fun openLog() {
        val file = logRepository.get()
        if (file == null) {
            JOptionPane.showMessageDialog(
                this,
                "The log is empty: startup actions have not run yet.",
                "Startup actions log",
                JOptionPane.INFORMATION_MESSAGE,
            )
            return
        }

        try {
            Desktop.getDesktop().open(file)
        } catch (ex: Exception) {
            logger.error(ex) { "Can not open startup actions log '${file.absolutePath}'" }
            showError("Can not open the log file:\n${file.absolutePath}\n\nReason: ${ex.message}")
        }
    }

    private fun showError(message: String) {
        JOptionPane.showMessageDialog(this, message, "Startup actions", JOptionPane.ERROR_MESSAGE)
    }

    private class StartupActionTableModel(
        private val editor: StartupActionsEditor,
    ) : AbstractTableModel() {
        override fun getRowCount(): Int = editor.entries.size

        override fun getColumnCount(): Int = COLUMNS.size

        override fun getColumnName(column: Int): String = COLUMNS[column]

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val entry: StartupActionEntry = editor.entries[rowIndex]
            return when (columnIndex) {
                0 -> rowIndex + 1
                1 -> editor.actionName(entry.actionId)
                2 -> if (editor.isResolved(entry)) editor.actionType(entry.actionId) else "?"
                3 -> if (entry.enabled) "yes" else "no"
                4 -> entry.input.orEmpty()
                5 -> editor.statusOf(entry.id).name
                else -> ""
            }
        }

        companion object {
            private val COLUMNS = listOf("#", "Action", "Type", "Enabled", "Input", "Status")
        }
    }
}
