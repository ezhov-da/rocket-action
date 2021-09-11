package ru.ezhov.rocket.action.types.gist

import org.eclipse.egit.github.core.Gist
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
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

class GistRocketAction : AbstractRocketAction() {

    override fun description(): String {
        return "Github gist loader"
    }

    override fun name(): String = "Работа с Gists"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "Label", true),
                createRocketActionProperty(
                        TOKEN,
                        TOKEN,
                        "Используйте это свойство или свойство командной строки Java -D$TOKEN_PROPERTY",
                        false
                ),
                createRocketActionProperty(USERNAME, USERNAME, "", true),
                createRocketActionProperty(
                        BASE_GIST_URL,
                        BASE_GIST_URL,
                        "Url gists",
                        false
                )
        )
    }

    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
                geToken(settings)?.takeIf { it.isNotEmpty() }?.let { token ->
                    settings.settings()[USERNAME]?.takeIf { it.isNotEmpty() }?.let { username ->
                        settings.settings()[BASE_GIST_URL]?.let { url ->
                            val menu = JMenu(label)
                            GistWorker(menu, gistUrl = url, token = token, username = username).execute()

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
                    ?: System.getProperty(TOKEN_PROPERTY, "").takeIf { it.isNotEmpty() }


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
            menu.icon = IconRepositoryFactory.repository.by(AppIcon.BOOKMARK)
            try {
                menu.removeAll()
                menu.add(this.get())
                NotificationFactory.notification.show(NotificationType.INFO, "Gists загружены")
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
            val buttonUpdate = JButton(IconRepositoryFactory.repository.by(AppIcon.RELOAD))
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

    companion object {
        const val LABEL = "label"
        const val TOKEN = "gistToken"
        const val USERNAME = "username"
        const val BASE_GIST_URL = "baseGistUrl"

        const val TOKEN_PROPERTY = "rocket.action.gist.token"
    }
}