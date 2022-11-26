package ru.ezhov.rocket.action.plugin.gist

import org.eclipse.egit.github.core.Gist
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import javax.swing.DefaultListModel
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

class GistRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

        override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

        override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun description(): String {
        return "Github gist loader"
    }

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL)

    override fun name(): String = "Работа с Gists"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = LABEL, name = LABEL.value, description = "Label", required = true),
            createRocketActionProperty(
                key = TOKEN,
                name = TOKEN.value,
                description = "Используйте это свойство или свойство командной строки Java -D$TOKEN_PROPERTY",
                required = false
            ),
            createRocketActionProperty(key = USERNAME, name = USERNAME.value, description = "", required = true),
            createRocketActionProperty(
                BASE_GIST_URL,
                BASE_GIST_URL.value,
                "Url gists",
                false
            )
        )
    }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
            actionContext = context
            geToken(settings)?.takeIf { it.isNotEmpty() }?.let { token ->
                settings.settings()[USERNAME]?.takeIf { it.isNotEmpty() }?.let { username ->
                    settings.settings()[BASE_GIST_URL]?.let { url ->
                        val menu = JMenu(label)
                        GistWorker(menu = menu, gistUrl = url, token = token, username = username).execute()

                        object : RocketAction {
                            override fun contains(search: String): Boolean = false

                            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                !(settings.id() == actionSettings.id() &&
                                    settings.settings() == actionSettings.settings())

                            override fun component(): Component = menu
                        }
                    }
                }
            }
        }

    private fun geToken(settings: RocketActionSettings) =
        settings.settings()[TOKEN]?.takeIf { it.isNotEmpty() }
            ?: System.getProperty(TOKEN_PROPERTY.value, "").takeIf { it.isNotEmpty() }


    override fun type(): RocketActionType = RocketActionType { "GIST" }

    private inner class GistWorker(
        private val menu: JMenu,
        private val gistUrl: String,
        private val token: String,
        private val username: String
    ) : SwingWorker<GistPanel, String?>() {

        @Throws(Exception::class)
        override fun doInBackground(): GistPanel {
            val gistActionService = GistActionService()
            val gists = gistActionService.gists(token = token, username = username)
            return GistPanel(menu, gists, gistUrl = gistUrl, token = token, username = username)
        }

        override fun done() {
            menu.icon = actionContext!!.icon().by(AppIcon.BOOKMARK)
            try {
                menu.removeAll()
                menu.add(this.get())
                actionContext!!.notification().show(NotificationType.INFO, "Gists загружены")
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

    private inner class GistPanel(
        menu: JMenu,
        gists: List<Gist>,
        gistUrl: String,
        token: String,
        username: String,
    ) : JPanel(BorderLayout()) {
        private val gistItems: MutableList<GistListItem>
        private val textFieldSearch = JTextField()
        private val model = DefaultListModel<GistListItem>()
        private fun fillAndSetModel(searchText: String) {
            model.removeAllElements()
            if ("" == searchText) {
                gistItems.forEach(Consumer { element: GistListItem -> model.addElement(element) })
            } else {
                gistItems.forEach(Consumer { i: GistListItem ->
                    if (i.name.contains(searchText)) {
                        model.addElement(i)
                    }
                })
            }
        }

        private inner class GistListItem(val name: String, val url: String) {
            override fun toString(): String {
                return name
            }
        }

        init {
            gistItems = ArrayList()
            for (gist in gists) {
                val fileMap = gist.files
                for ((key) in fileMap) {
                    gistItems.add(GistListItem(key, gist.htmlUrl))
                }
            }
            gistItems.sortedBy { it.name }
            gistItems.forEach(Consumer { element: GistListItem -> model.addElement(element) })
            val list = JList(model)
            list.addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(e: MouseEvent) {
                    val value = list.selectedValue
                    if (value != null && Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(URI(value.url))
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        } catch (ex: URISyntaxException) {
                            ex.printStackTrace()
                        }
                    }
                }
            })
            val panelSearchAndUpdate = JPanel(BorderLayout())
            val buttonUpdate = JButton(actionContext!!.icon().by(AppIcon.RELOAD))
            buttonUpdate.addActionListener { GistWorker(menu, gistUrl = gistUrl, token = token, username = username).execute() }
            panelSearchAndUpdate.add(textFieldSearch, BorderLayout.CENTER)
            panelSearchAndUpdate.add(buttonUpdate, BorderLayout.EAST)
            add(panelSearchAndUpdate, BorderLayout.NORTH)
            add(JScrollPane(list), BorderLayout.CENTER)
            if ("" != gistUrl) {
                val label = JLabel(gistUrl)
                add(label, BorderLayout.SOUTH)
                label.addMouseListener(object : MouseAdapter() {
                    override fun mouseReleased(e: MouseEvent) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(URI(gistUrl))
                            } catch (ex: IOException) {
                                ex.printStackTrace()
                            } catch (ex: URISyntaxException) {
                                ex.printStackTrace()
                            }
                        }
                    }
                })
            }
            textFieldSearch.addActionListener { SwingUtilities.invokeLater { fillAndSetModel(textFieldSearch.text) } }
        }
    }

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.BOOKMARK)

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val TOKEN = RocketActionConfigurationPropertyKey("gistToken")
        private val USERNAME = RocketActionConfigurationPropertyKey("username")
        private val BASE_GIST_URL = RocketActionConfigurationPropertyKey("baseGistUrl")
        private val TOKEN_PROPERTY = RocketActionConfigurationPropertyKey("rocket.action.gist.token")
    }
}
