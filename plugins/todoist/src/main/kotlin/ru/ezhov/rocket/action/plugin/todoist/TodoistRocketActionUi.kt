package ru.ezhov.rocket.action.plugin.todoist

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
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
import java.util.*
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
    private var actionContext: RocketActionContext? = null

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-todoist.properties"))
        object : RocketActionPluginInfo {
            override fun version(): String = properties.getProperty("version")

            override fun author(): String = properties.getProperty("author")

            override fun link(): String? = properties.getProperty("link")
        }
    }

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun description(): String {
        return "Simple todoist client"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = LABEL, name = LABEL, description = "Header", required = true),
            createRocketActionProperty(
                key = TOKEN,
                name = TOKEN,
                description = "Use this property or Java property from command line -D${TOKEN_PROPERTY}",
                required = true
            )
        )
    }

    override fun asString(): List<String> = listOf(LABEL)

    override fun name(): String = "Working with Todoist"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.BOOKMARK)

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
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
            ?: System.getProperty(TOKEN_PROPERTY, "").takeIf { it.isNotEmpty() }

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
            menu.icon = actionContext!!.icon().by(AppIcon.BOOKMARK)
            try {
                menu.removeAll()
                menu.add(this.get())
                actionContext!!.notification().show(NotificationType.INFO, "Todoist loaded")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }

        init {
            menu.removeAll()
            menu.icon = ImageIcon(this.javaClass.getResource("/icons/load_16x16.gif"))
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
                override fun getListCellRendererComponent(
                    list: JList<*>?,
                    value: Any,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
                ): Component {
                    val label =
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
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
        private val LABEL = "label"
        private val TOKEN = "todoistToken"
        private val TOKEN_PROPERTY = "rocket.action.toodoist.token"
    }
}
