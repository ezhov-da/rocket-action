package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import arrow.core.flatten
import mu.KotlinLogging
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
import java.awt.Component
import java.awt.Desktop
import java.awt.FlowLayout
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import java.net.URI
import java.time.format.DateTimeFormatter
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.JTextPane
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
    private val tasks: List<Task> = emptyList(),
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
        add(
            JMenuBar()
                .apply {
                    linkToWorkLog?.let { ltwl ->
                        add(
                            JButton("Open link").apply {
                                addActionListener {
                                    try {
                                        Desktop.getDesktop().browse(ltwl)
                                    } catch (ex: Exception) {
                                        val msg = "Error open link='$ltwl'"
                                        logger.warn(ex) { msg }
                                        context.notification().show(type = NotificationType.WARN, text = msg)
                                    }
                                }
                            }
                        )
                    }
                    createAliasMenu(tasks = tasks, aliasForTaskIds = aliasForTaskIds) { task ->
                        textPane.document.insertString(
                            textPane.caretPosition, task.id, null
                        )
                    }?.let { aliasesMenu -> add(aliasesMenu) }


                    createSubstitutionPatternsMenu(aliasForTaskIds) { value ->
                        textPane.document.insertString(
                            textPane.caretPosition, value, null
                        )
                    }?.let { patternsMenu -> add(patternsMenu) }
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

    private fun createAliasMenu(
        tasks: List<Task>,
        aliasForTaskIds: AliasForTaskIds,
        addCallback: (task: Task) -> Unit,
    ): JMenu? = tasks.takeIf { it.isNotEmpty() }?.let {

        val jMenu = JMenu("Favorite tasks")
        tasks.forEach { task ->
            val aliasesText = aliasForTaskIds
                .aliasesBy(task.id)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "[", postfix = "]", separator = ", ")
                .orEmpty()

            jMenu.add(
                JMenuItem("${task.id} $aliasesText - ${task.name}").apply {
                    addActionListener {
                        SwingUtilities.invokeLater { addCallback(task) }
                    }
                })
        }
        jMenu
    }

    private fun createSubstitutionPatternsMenu(
        aliasForTaskIds: AliasForTaskIds,
        addCallback: (text: String) -> Unit,
    ): JMenu? = aliasForTaskIds
        .values
        .takeIf { it.isNotEmpty() }
        ?.let { values ->
            val jMenu = JMenu("Substitution patterns")
            val patterns = values.map { (key, values) ->
                values.map { it to key }
            }
                .flatten()
                .toMap()


            patterns.forEach { (k, v) ->
                jMenu.add(
                    JMenuItem("$k - $v").apply {
                        addActionListener {
                            SwingUtilities.invokeLater { addCallback(k) }
                        }
                    })
            }
            jMenu
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
        override fun doInBackground() {
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

    private class TasksPanel(
        private val preparedTasks: List<Task>,
        private val commitTimeService: CommitTimeService,
        private val context: RocketActionContext,
        commitTimeTaskInfoRepository: CommitTimeTaskInfoRepository,
        private val maxTimeInMinutes: Int?,
    ) : JPanel() {
        private val labelInfo: JLabel = JLabel()
        private val errorsInfo: JTextPane = JTextPane()
        private val tableModel = object : DefaultTableModel() {
            override fun getColumnClass(columnIndex: Int): Class<TableTasksPanelTask> =
                TableTasksPanelTask::class.java
        }
        private val table = object : JTable(tableModel) {
            override fun isCellEditable(row: Int, column: Int): Boolean = false


        }
        private var currentTasks: MutableList<TableTasksPanelTask> = mutableListOf()
        private val buttonCommit: JButton = JButton("Contribute")

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
                JPanel(FlowLayout(FlowLayout.LEFT))
                    .apply {
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
                        add(labelInfo)
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
            add(buttonCommit, BorderLayout.SOUTH)
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

            errorsInfo.isEditable = false
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
