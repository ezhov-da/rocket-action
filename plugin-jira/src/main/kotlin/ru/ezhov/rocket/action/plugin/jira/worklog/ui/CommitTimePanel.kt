package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import arrow.core.getOrHandle
import mu.KotlinLogging
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTasks
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

private val logger = KotlinLogging.logger {}

class CommitTimePanel(
    private val tasks: List<Task> = emptyList(),
    private val commitTimeService: CommitTimeService,
) : JPanel() {
    private val textPane: JTextPane = JTextPane()
    private val infoLabel: JTextArea = JTextArea().apply { text = "Введите данные для расчёта" }
    private val buttonCommit: JButton = JButton("Зафиксировать")
    private var currentCommitTimeTasks: CommitTimeTasks? = null

    init {
        infoLabel.isEditable = false

        layout = BorderLayout()
        add(
            FavoriteTasksPanel(tasks) { task ->
                textPane.document.insertString(textPane.document.length, "${task.id}\n", null);
            },
            BorderLayout.NORTH
        )
        add(JScrollPane(textPane), BorderLayout.CENTER)
        add(JPanel(BorderLayout()).apply {
            add(infoLabel, BorderLayout.CENTER)
            add(buttonCommit, BorderLayout.SOUTH)
        }, BorderLayout.SOUTH)

        buttonCommit.addActionListener {
            currentCommitTimeTasks?.let { tasks ->
                CommitWorker(
                    commitTimeTasks = tasks,
                    commitTimeService = commitTimeService,
                    button = buttonCommit
                ).execute()
            }
        }

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
                currentCommitTimeTasks = CommitTimeTasks.of(textPane.text)
                printInfo(currentCommitTimeTasks!!)
            }
        })
    }

    private class CommitWorker(
        private val commitTimeTasks: CommitTimeTasks,
        private val commitTimeService: CommitTimeService,
        private val button: JButton,
    ) : SwingWorker<Unit, String>() {

        init {
            button.isEnabled = false
        }

        override fun doInBackground(): Unit =
            commitTimeService
                .commit(commitTimeTasks.commitTimeTask)
                .getOrHandle { ex -> throw ex }

        override fun done() {
            try {
                get()
                NotificationFactory.notification.show(
                    type = NotificationType.INFO,
                    text = "Tasks time added to Jira"
                )
            } catch (ex: Exception) {
                val text = "Error when commit time"
                logger.error(ex) { text }
                NotificationFactory.notification.show(type = NotificationType.WARN, text = text)
            } finally {
                button.isEnabled = true
            }
        }
    }

    private fun printInfo(tasks: CommitTimeTasks) {
        infoLabel.text = """
                Число задач: '${tasks!!.countOfTask()}'
                Суммарное время:
                    - в минутах ${tasks!!.sumOfTimeTasksAsMinute()}
                    - в часах ${tasks!!.sumOfTimeTasksAsHours()}
                ${if (tasks.hasErrors()) "Ошибки:" else ""}
                ${tasks.errors.joinToString { "${it.message}\n" }}
            """.trimIndent()
    }


    private class FavoriteTasksPanel(
        private val tasks: List<Task>,
        private val addCallback: (task: Task) -> Unit,
    ) : JPanel() {
        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            tasks.forEach {
                add(FavoriteTaskPanel(task = it, addCallback = addCallback))
            }
        }
    }

    private class FavoriteTaskPanel(
        private val task: Task,
        private val addCallback: (task: Task) -> Unit,
    ) : JPanel() {
        private val textField = JTextField()
        private val addButton = JButton("+")

        init {
            layout = BorderLayout()
            textField.isEditable = false
            textField.text = "${task.id} - ${task.name}"
            add(textField, BorderLayout.CENTER)
            add(addButton, BorderLayout.EAST)

            addButton.addActionListener {
                SwingUtilities.invokeLater { addCallback(task) }
            }
        }
    }
}
