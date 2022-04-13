package ru.ezhov.rocket.action.plugin.todoist

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.plugin.todoist.model.Project
import ru.ezhov.rocket.action.plugin.todoist.model.Task
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.ExecutionException
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingWorker

class TodoistRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private val icon = IconRepositoryFactory.repository.by(AppIcon.BOOKMARK)

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun description(): String {
        return "Simple todoist client"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = LABEL, name = LABEL.value, description = "Заголовок", required = true),
            createRocketActionProperty(
                key = TOKEN,
                name = TOKEN.value,
                description = "Используйте это свойство или свойство Java из командной строки -D${TOKEN_PROPERTY.value}",
                required = true
            )
        )
    }

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL)

    override fun name(): String = "Работа с Todois"

    override fun icon(): Icon? = icon

    override fun create(settings: RocketActionSettings): RocketAction? =
        getToken(settings)?.takeIf { it.isNotEmpty() }?.let { token ->
            settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
                val menu = JMenu(label)
                TodoistWorker(menu, token = token).execute()

                object : RocketAction {
                    override fun contains(search: String): Boolean = false

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = menu
                }
            }
        }

    private fun getToken(settings: RocketActionSettings) =
        settings.settings()[TOKEN]?.takeIf { it.isNotEmpty() }
            ?: System.getProperty(TOKEN_PROPERTY.value, "").takeIf { it.isNotEmpty() }

    override fun type(): RocketActionType = RocketActionType { "TODOIST" }

    private inner class TodoistWorker(
        private val menu: JMenu,
        private val token: String,
    ) : SwingWorker<TodoistPanel, String?>() {

        @Throws(Exception::class)
        override fun doInBackground(): TodoistPanel {
            return TodoistPanel(token = token)
        }

        override fun done() {
            menu.icon = icon
            try {
                menu.removeAll()
                menu.add(this.get())
                NotificationFactory.notification.show(NotificationType.INFO, "Todoist загружен")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }

        init {
            menu.removeAll()
            menu.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
        }
    }

    private inner class TodoistPanel(
        projects: List<Project> = emptyList(),
        token: String,
    ) : JPanel(BorderLayout()) {
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
                        if (labelUrl.text.isNotEmpty()) {
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
                val tasks = todoistTaskRepository.tasks(token)
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
            taskJList.addListSelectionListener {
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
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val TOKEN = RocketActionConfigurationPropertyKey("todoistToken")
        private val TOKEN_PROPERTY = RocketActionConfigurationPropertyKey("rocket.action.toodoist.token")
    }
}
