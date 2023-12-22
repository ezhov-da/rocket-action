package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import arrow.core.flatten
import mu.KotlinLogging
import org.jdesktop.swingx.JXCollapsiblePane
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeServiceException
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeTaskInfoException
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeTaskInfoRepository
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.AliasForTaskIds
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTask
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTaskInfo
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTasks
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.Validator
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.net.URI
import java.time.format.DateTimeFormatter
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.JTextPane
import javax.swing.JToolBar
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.undo.CannotRedoException
import javax.swing.undo.CannotUndoException
import javax.swing.undo.UndoManager


private val logger = KotlinLogging.logger {}

class CommitTimePanel(
    tasks: List<Task> = emptyList(),
    private val delimiter: String,
    private val dateFormatPattern: String,
    private val constantsNowDate: List<String>,
    private val aliasForTaskIds: AliasForTaskIds,
    private val fileForSave: File,
    private val context: RocketActionContext,
    private val validator: Validator,
    commitTimeService: CommitTimeService,
    commitTimeTaskInfoRepository: CommitTimeTaskInfoRepository,
    linkToWorkLog: URI? = null,
    maxTimeInMinutes: Int?,
) : JPanel() {
    private val textPane: JTextPane = JTextPane()
    private var currentCommitTimeTasks: CommitTimeTasks? = null
    private val tasksPanel: TasksPanel = TasksPanel(
        preparedTasks = tasks,
        commitTimeService = commitTimeService,
        commitTimeTaskInfoRepository = commitTimeTaskInfoRepository,
        context = context,
        maxTimeInMinutes = maxTimeInMinutes,
    )

    init {
        ReadFile(textPane = textPane, file = fileForSave, context = context).execute()
        layout = BorderLayout()

        val aliasForTaskIdsPanel = AliasForTaskIdsPanel(aliasForTaskIds)
        val favoriteTasksPanel = FavoriteTasksPanel.create(tasks = tasks, aliasForTaskIds = aliasForTaskIds) { task ->
            textPane.document.insertString(
                textPane.caretPosition, task.id, null
            )
        }
        add(
            JPanel(BorderLayout())
                .apply {
                    val toolBar = JToolBar().apply { isFloatable = false }
                    linkToWorkLog?.let { ltwl ->
                        toolBar.add(
                            JButton("Open link").apply {
                                addMouseListener(object : MouseAdapter() {
                                    override fun mouseReleased(e: MouseEvent?) {
                                        try {
                                            Desktop.getDesktop().browse(ltwl)
                                        } catch (ex: Exception) {
                                            val msg = "Error open link='$ltwl'"
                                            logger.warn(ex) { msg }
                                            context.notification().show(type = NotificationType.WARN, text = msg)
                                        }
                                    }
                                })
                            }
                        )

                        toolBar.add(JButton("Show substitution patterns").apply {
                            addActionListener {
                                SwingUtilities.invokeLater {
                                    aliasForTaskIdsPanel.isCollapsed = !aliasForTaskIdsPanel.isCollapsed
                                }
                            }
                        })
                    }

                    val innerPanel = JPanel(BorderLayout()).apply {
                        add(aliasForTaskIdsPanel, BorderLayout.CENTER)
                        favoriteTasksPanel?.let {
                            add(it, BorderLayout.NORTH)
                        }
                    }

                    add(toolBar, BorderLayout.NORTH)
                    add(innerPanel, BorderLayout.CENTER)
                },
            BorderLayout.NORTH
        )

        setUndoAndRedo()

        val split = JSplitPane(JSplitPane.VERTICAL_SPLIT, JScrollPane(textPane), tasksPanel)
            .apply {
                // https://stackoverflow.com/questions/7625762/setting-divider-location-on-a-jsplitpane-doesnt-work
                // put everything about resize, size, whatever for JSplitPane into invokeLater() .
                SwingUtilities.invokeLater { setDividerLocation(0.5) }
            }

        add(split, BorderLayout.CENTER)

        textPane.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                calculateAndPrintInfo()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                calculateAndPrintInfo()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                calculateAndPrintInfo()
            }

            private fun calculateAndPrintInfo() {
                currentCommitTimeTasks = CommitTimeTasks.of(
                    value = textPane.text,
                    delimiter = delimiter,
                    dateFormatPattern = dateFormatPattern,
                    constantsNowDate = constantsNowDate,
                    aliasForTaskIds = aliasForTaskIds,
                    validator = validator,
                )
                tasksPanel.setCurrentCommitTimeTasks(currentCommitTimeTasks!!)
            }
        })

        textPane.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == 83 /*S*/ && e.isControlDown) {
                    WriteFile(text = textPane.text, file = fileForSave, context = context).execute()
                }
            }
        })
    }

    private fun setUndoAndRedo() {
        val manager = UndoManager()
        textPane.document.addUndoableEditListener(manager)
        val undoAction: Action = UndoAction(manager)
        val redoAction: Action = RedoAction(manager)
        textPane.registerKeyboardAction(
            undoAction,
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK),
            WHEN_FOCUSED
        )
        textPane.registerKeyboardAction(
            redoAction,
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_MASK + InputEvent.CTRL_MASK),
            WHEN_FOCUSED
        )
    }

    fun loadText() {
        ReadFile(textPane = textPane, file = fileForSave, context = context).execute()
    }

    fun appendTextToCurrentAndSave(text: String) {
        SwingUtilities.invokeLater {
            textPane.document.insertString(textPane.text.length, "\n$text", null)
            WriteFile(text = textPane.text, file = fileForSave, context = context).execute()
        }
    }

    class WriteFile(
        private val text: String,
        private val file: File,
        private val context: RocketActionContext,
    ) : SwingWorker<Any, Unit>() {
        override fun doInBackground(): Unit {
            file.parentFile?.let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
            file.writeText(text, charset = Charsets.UTF_8)
        }

        override fun done() {
            try {
                context.notification().show(NotificationType.INFO, "Text saved")
            } catch (ex: Exception) {
                logger.warn(ex) { "Error when save text" }
                context.notification().show(NotificationType.WARN, "Save error")

            }
        }
    }

    class ReadFile(
        private val textPane: JTextPane,
        private val file: File,
        private val context: RocketActionContext,
    ) : SwingWorker<String, String>() {
        override fun doInBackground(): String =
            if (file.exists()) {
                file.readText(charset = Charsets.UTF_8)
            } else {
                ""
            }

        override fun done() {
            try {
                val text = this.get()
                textPane.text = text
                context.notification().show(NotificationType.INFO, "Text is loaded")
            } catch (ex: Exception) {
                logger.warn(ex) { "Error when get text" }
                context.notification().show(NotificationType.WARN, "Error loaded text")

            }
        }
    }

    private class CommitWorker(
        private val commitTimeTasks: List<TableTasksPanelTask>,
        private val commitTimeService: CommitTimeService,
        private val button: JButton,
        private val context: RocketActionContext,
        private val afterCommitCallBack: (Pair<TableTasksPanelTask, CommitTimeServiceException?>) -> Unit,
    ) : SwingWorker<Unit, Pair<TableTasksPanelTask, CommitTimeServiceException?>>() {

        init {
            button.isEnabled = false
        }

        override fun doInBackground() {
            commitTimeTasks.forEach { task ->
                publish(
                    commitTimeService
                        .commit(task = task.task)
                        .fold(ifLeft = { ex -> task to ex }, ifRight = { task to null })
                )
            }
        }

        override fun process(chunks: MutableList<Pair<TableTasksPanelTask, CommitTimeServiceException?>>) {
            chunks.forEach { afterCommitCallBack(it) }
        }

        override fun done() {
            try {
                get()
                context.notification().show(
                    type = NotificationType.INFO,
                    text = "Tasks time added to Jira"
                )
            } catch (ex: Exception) {
                val text = "Error when commit time"
                logger.error(ex) { text }
                context.notification().show(type = NotificationType.WARN, text = text)
            } finally {
                button.isEnabled = true
            }
        }
    }

    private class NameWorker(
        private val commitTimeTasks: List<TableTasksPanelTask>,
        private val commitTimeTaskInfoRepository: CommitTimeTaskInfoRepository,
        private val button: JButton,
        private val afterSearchName: (Triple<TableTasksPanelTask, CommitTimeTaskInfo?, CommitTimeTaskInfoException?>) -> Unit,
        private val context: RocketActionContext,
    ) : SwingWorker<Unit, Triple<TableTasksPanelTask, CommitTimeTaskInfo?, CommitTimeTaskInfoException?>>() {

        init {
            button.isEnabled = false
        }

        override fun doInBackground() {
            commitTimeTasks.forEach { task ->
                publish(
                    commitTimeTaskInfoRepository
                        .info(id = task.task.id)
                        .fold(
                            ifLeft = { ex -> Triple(task, null, ex) },
                            ifRight = { result ->
                                Triple(first = task, second = result, third = null)
                            })
                )
            }
        }

        override fun process(
            chunks: MutableList<Triple<TableTasksPanelTask, CommitTimeTaskInfo?, CommitTimeTaskInfoException?>>
        ) {
            chunks.forEach { afterSearchName(it) }
        }

        override fun done() {
            try {
                get()
                context.notification().show(
                    type = NotificationType.INFO,
                    text = "Names search complete"
                )
            } catch (ex: Exception) {
                val text = "Names search error"
                logger.error(ex) { text }
                context.notification().show(type = NotificationType.WARN, text = text)
            } finally {
                button.isEnabled = true
            }
        }
    }

    private class AliasForTaskIdsPanel(
        aliasForTaskIds: AliasForTaskIds,
    ) : JXCollapsiblePane() {
        private val textPane = JTextPane()

        init {
            isCollapsed = true
            layout = BorderLayout()
            border = BorderFactory.createTitledBorder("Substitution patterns")
            textPane.isEditable = false
            add(textPane, BorderLayout.CENTER)
            textPane.text = aliasForTaskIds.values.map { (key, values) ->
                values.map { "$it - $key" }
            }
                .flatten()
                .joinToString(separator = "\n")
        }
    }

    private class TasksPanel(
        private val preparedTasks: List<Task>,
        private val commitTimeService: CommitTimeService,
        private val context: RocketActionContext,
        commitTimeTaskInfoRepository: CommitTimeTaskInfoRepository,
        private val maxTimeInMinutes: Int?,
    ) : JPanel() {
        private val labelInfo: JLabel = JLabel()
        private val errorsInfo: JTextPane = JTextPane().apply { isEditable = false }
        private val tableModel = object : DefaultTableModel() {
            override fun getColumnClass(columnIndex: Int): Class<TableTasksPanelTask> =
                TableTasksPanelTask::class.java
        }
        private val table = object : JTable(tableModel) {
            override fun isCellEditable(row: Int, column: Int): Boolean = false


        }
        private var currentTasks: MutableList<TableTasksPanelTask> = mutableListOf()
        private val buttonCommit: JButton = JButton(
            "Contribute",
            ImageIcon(TasksPanel::class.java.getResource("/jira/upload_16x16.png"))
        ).apply {
            background = Color.GREEN
        }

        private val taskNamesCache = mutableMapOf<String, String>()

        init {
            layout = BorderLayout()
            table.tableHeader.reorderingAllowed = false
            table.setDefaultRenderer(
                TableTasksPanelTask::class.java,
                object : DefaultTableCellRenderer() {
                    override fun getTableCellRendererComponent(
                        table: JTable,
                        value: Any?,
                        isSelected: Boolean,
                        hasFocus: Boolean,
                        row: Int,
                        column: Int
                    ): Component {
                        val label = super.getTableCellRendererComponent(
                            table,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            column
                        ) as JLabel
                        when (value) {
                            is TableTasksPanelTask -> {
                                label.let { l ->
                                    when (column) {
                                        0 -> l.text = value.task.id
                                        1 -> l.text = value.name.orEmpty()
                                        2 -> l.text =
                                            value.task.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                                        3 -> l.text = value.task.timeSpentMinute.toString()
                                        4 -> l.text = value.task.comment
                                        5 -> l.text = value.status.name
                                    }
                                }
                            }
                        }

                        return label
                    }
                }
            )

            tableModel.addColumn("ID")
            tableModel.addColumn("Name")
            tableModel.addColumn("Date")
            tableModel.addColumn("Time in minutes")
            tableModel.addColumn("A comment")
            tableModel.addColumn("Status")

            buttonCommit.addActionListener {
                val answer = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to record the time write-off?",
                    "Contribute?",
                    JOptionPane.YES_NO_OPTION,
                )

                if (answer != JOptionPane.YES_OPTION) return@addActionListener

                currentTasks
                    .takeIf { it.isNotEmpty() }
                    ?.let { tasks ->
                        CommitWorker(
                            commitTimeTasks = tasks.toList(),
                            commitTimeService = commitTimeService,
                            button = buttonCommit,
                            context = context,
                        ) { result ->
                            result.second?.let { ex ->
                                logger.warn(ex) { "Error when commit time for task ${result.first}" }
                                result.first.status = Status.ERROR
                            } ?: run {
                                result.first.status = Status.COMMITTED
                            }
                            tableModel.fireTableDataChanged()
                        }
                            .execute()
                    }
            }

            add(
                JPanel(BorderLayout()).apply {
                    val infoPanel = JPanel().apply {
                        add(labelInfo)
                    }

                    val buttonsPanel = JPanel().apply {
                        layout = BoxLayout(this, BoxLayout.X_AXIS)
                        add(JButton("Pull up task names")
                            .also { button ->
                                button.addActionListener {
                                    currentTasks
                                        .filter { it.name == null }
                                        .let { tasks ->
                                            NameWorker(
                                                commitTimeTasks = tasks,
                                                commitTimeTaskInfoRepository = commitTimeTaskInfoRepository,
                                                button = button,
                                                context = context,
                                                afterSearchName = { triple ->
                                                    triple.third
                                                        ?.let { ex ->
                                                            logger.warn(ex) { "Error get name for task ${triple.first}" }
                                                        } ?: run {
                                                        triple.second?.let { info ->
                                                            taskNamesCache[triple.first.task.id] = info.name
                                                            logger.debug {
                                                                "Put task name to cache '${triple.first.task.id}'='${info.name}'"
                                                            }
                                                            triple.first.name = info.name
                                                            SwingUtilities.invokeLater { tableModel.fireTableDataChanged() }
                                                        }
                                                    }
                                                }
                                            ).execute()
                                        }
                                }
                            })

                        add(Box.createHorizontalGlue())
                        add(buttonCommit)
                    }

                    add(infoPanel, BorderLayout.NORTH)
                    add(buttonsPanel, BorderLayout.CENTER)
                    border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
                },
                BorderLayout.NORTH
            )
            add(
                JPanel(BorderLayout())
                    .apply {
                        add(errorsInfo, BorderLayout.NORTH)
                        add(JScrollPane(table), BorderLayout.CENTER)
                    },
                BorderLayout.CENTER
            )
        }

        fun setCurrentCommitTimeTasks(tasks: CommitTimeTasks) {
            val textDiff = if (maxTimeInMinutes != null) {
                " It remains to specify <b>'${maxTimeInMinutes - tasks.sumOfTimeTasksAsMinute()}'</b> " +
                    "minutes from <b>'$maxTimeInMinutes'</b>."
            } else {
                ""
            }

            labelInfo.text = "<html>Number of tasks: <b>${tasks.countOfTask()}</b>. " +
                "Total time: in minutes <b>${tasks.sumOfTimeTasksAsMinute()}</b>, " +
                "in hours <b>${tasks.sumOfTimeTasksAsHours()}</b>. $textDiff"

            while (tableModel.rowCount != 0) {
                tableModel.removeRow(tableModel.rowCount - 1)
            }
            currentTasks.clear()
            currentTasks = tasks
                .commitTimeTask
                .map { task ->
                    TableTasksPanelTask(
                        task = task,
                        name = preparedTasks
                            .find { pt -> pt.id == task.id }?.name ?: taskNamesCache[task.id]
                    )
                }.toMutableList()

            currentTasks.forEach {
                tableModel.addRow(listOf(it, it, it, it, it, it).toTypedArray())
            }

            errorsInfo.text = tasks.errors.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n") { it.message }
                ?: ""
        }
    }

    private data class TableTasksPanelTask(
        val task: CommitTimeTask,
        var status: Status = Status.PREPARED,
        var name: String? = null,
    )

    private enum class Status { COMMITTED, ERROR, PREPARED }
}

internal class UndoAction(private val manager: UndoManager) : AbstractAction() {
    override fun actionPerformed(evt: ActionEvent) {
        try {
            manager.undo()
        } catch (e: CannotUndoException) {
            Toolkit.getDefaultToolkit().beep()
        }
    }
}

internal class RedoAction(private val manager: UndoManager) : AbstractAction() {
    override fun actionPerformed(evt: ActionEvent) {
        try {
            manager.redo()
        } catch (e: CannotRedoException) {
            Toolkit.getDefaultToolkit().beep()
        }
    }
}
