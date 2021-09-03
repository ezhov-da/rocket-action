package ru.ezhov.rocket.action.types.todoist

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.types.ConfigurationUtil
import ru.ezhov.rocket.action.types.todoist.model.Project
import ru.ezhov.rocket.action.types.todoist.model.Task
import java.awt.*
import java.awt.event.*
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.ExecutionException
import javax.swing.*
import javax.swing.event.ListSelectionEvent

class TodoistRocketAction : AbstractRocketAction() {
    private var menu: JMenu? = null
    override fun description(): String {
        return "Simple todoist client"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, "Label", true),
                createRocketActionProperty(
                        TOKEN,
                        "Use this or -D" + TodoistProjectRepository.TOKEN_PROPERTY,
                        false
                )
        )
    }

    override fun create(settings: RocketActionSettings): Component {
        menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
        TodoistWorker(settings).execute()
        return menu!!
    }

    override fun type(): String {
        return "TODOIST"
    }

    private inner class TodoistWorker(settings: RocketActionSettings) : SwingWorker<TodoistPanel, String?>() {
        private val settings: RocketActionSettings

        @Throws(Exception::class)
        override fun doInBackground(): TodoistPanel {
            return TodoistPanel(null, settings.settings()[BASE_GIST_URL], settings)
        }

        override fun done() {
            menu!!.icon = IconRepositoryFactory.repository.by(AppIcon.BOOKMARK)
            try {
                menu!!.removeAll()
                menu!!.add(this.get())
                NotificationFactory.notification.show(NotificationType.INFO, "Todoist loaded")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }

        init {
            menu!!.removeAll()
            menu!!.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
            this.settings = settings
        }
    }

    private inner class TodoistPanel(gists: List<Project?>?, gistUrl: String?, settings: RocketActionSettings) : JPanel(BorderLayout()) {
        private val todoistTaskRepository = TodoistTaskRepository()
        private val projectListModel = DefaultListModel<Project>()
        private val taskListModel = DefaultListModel<Task?>()
        private val panelTaskInfo: JPanelTaskInfo = JPanelTaskInfo()

        private inner class JPanelTaskInfo : JPanel(BorderLayout()) {
            private val labelId = JLabel()
            private val labelCreated = JLabel()
            private val labelUrl = JLabel()
            private val textPaneContent = JTextPane()
            fun setTask(task: Task) {
                labelUrl.text = task.url
                textPaneContent.text = task.content
            }

            init {
                labelUrl.addMouseListener(object : MouseAdapter() {
                    override fun mouseReleased(e: MouseEvent) {
                        if ("" != labelUrl.text) {
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().browse(URI(labelUrl.text))
                                } catch (ioException: IOException) {
                                    ioException.printStackTrace()
                                } catch (uriSyntaxException: URISyntaxException) {
                                    uriSyntaxException.printStackTrace()
                                }
                            }
                        }
                    }
                })
                add(JScrollPane(textPaneContent), BorderLayout.NORTH)
                add(labelUrl, BorderLayout.CENTER)
            }
        }

        init {
            try {
                val tasks = todoistTaskRepository.tasks(settings)
                taskListModel.removeAllElements()
                for (task in tasks) {
                    taskListModel.addElement(task)
                }
            } catch (e: TodoistRepositoryException) {
                e.printStackTrace()
            }
            val taskJList: JList<Task> = JList<Task>(taskListModel)
            taskJList.fixedCellWidth = 500
            taskJList.setCellRenderer(object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(list: JList<*>?, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                    label.text = (value as Task).content
                    return label
                }
            })
            taskJList.addListSelectionListener { e: ListSelectionEvent? ->
                val task = taskJList.selectedValue
                if (task != null) {
                    panelTaskInfo.setTask(task)
                }
            }
            val scrollPane = JScrollPane(taskJList)
            add(scrollPane, BorderLayout.CENTER)
            add(panelTaskInfo, BorderLayout.SOUTH)
        }
    }

    companion object {
        const val LABEL = "label"
        const val TOKEN = "todoistToken"
        const val BASE_GIST_URL = "baseGistUrl"
    }
}