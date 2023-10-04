package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.AliasForTaskIds
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ComboBoxModel
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel

class FavoriteTasksPanel(
    tasks: List<Task>,
    private val aliasForTaskIds: AliasForTaskIds,
    private val addCallback: (task: Task) -> Unit,
) : JPanel() {
    private var comboboxModel: ComboBoxModel<TaskComboBox>
    private val combobox: JComboBox<TaskComboBox> = JComboBox()
    private val button: JButton = JButton("Insert ID of selected task")

    init {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        border = BorderFactory.createTitledBorder("Favorite tasks")
        comboboxModel = DefaultComboBoxModel(tasks.map { TaskComboBox.create(it, aliasForTaskIds) }.toTypedArray())
        with(combobox) {
            maximumRowCount = 20
            model = comboboxModel
            renderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>,
                    value: Any?,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
                ): Component {
                    val label =
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                    (value as? TaskComboBox)?.let {
                        label.text = value.text
                    }
                    return label
                }
            }
        }

        button.apply {
            addActionListener {
                addCallback((comboboxModel.selectedItem as TaskComboBox).task)
            }
        }

        add(button)
        add(combobox)
    }

    companion object {
        fun create(
            tasks: List<Task>,
            aliasForTaskIds: AliasForTaskIds,
            addCallback: (task: Task) -> Unit,
        ): FavoriteTasksPanel? = tasks.takeIf { it.isNotEmpty() }?.let {
            FavoriteTasksPanel(
                tasks = tasks,
                aliasForTaskIds = aliasForTaskIds,
                addCallback = addCallback,
            )
        }
    }
}

private data class TaskComboBox(
    val text: String,
    val task: Task,
) {
    companion object {
        fun create(task: Task, aliasForTaskIds: AliasForTaskIds): TaskComboBox {
            val aliasesText = aliasForTaskIds
                .aliasesBy(task.id)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = " [", postfix = "] ", separator = ", ")
                .orEmpty()

            return TaskComboBox(
                text = "${task.id}$aliasesText- ${task.name}",
                task = task,
            )
        }
    }
}
