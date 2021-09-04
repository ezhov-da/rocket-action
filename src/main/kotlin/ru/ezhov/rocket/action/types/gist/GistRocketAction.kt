package ru.ezhov.rocket.action.types.gist

import org.eclipse.egit.github.core.Gist
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.types.ConfigurationUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import javax.swing.*

class GistRocketAction : AbstractRocketAction() {
    private var menu: JMenu? = null
    override fun description(): String {
        return "Github gist loader"
    }

    override fun name(): String = "Работа с Gists"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return Arrays.asList(
                createRocketActionProperty(LABEL, LABEL, "Label", true),
                createRocketActionProperty(
                        TOKEN,
                        TOKEN,
                        "Use this or -D" + GistActionService.TOKEN_PROPERTY,
                        false
                ),
                createRocketActionProperty(USERNAME, USERNAME, "", true),
                createRocketActionProperty(
                        BASE_GIST_URL,
                        BASE_GIST_URL,
                        "Url gists for open",
                        false
                )
        )
    }

    override fun create(settings: RocketActionSettings): Component {
        menu = JMenu(ConfigurationUtil.getValue(settings!!.settings(), LABEL))
        GistWorker(settings).execute()
        return menu!!
    }

    override fun type(): String {
        return "GIST"
    }

    private inner class GistWorker(settings: RocketActionSettings) : SwingWorker<GistPanel, String?>() {
        private val settings: RocketActionSettings

        @Throws(Exception::class)
        override fun doInBackground(): GistPanel {
            val gistActionService = GistActionService()
            val gists = gistActionService.gists(settings)
            return GistPanel(gists, settings.settings()[BASE_GIST_URL]!!, settings)
        }

        override fun done() {
            menu!!.icon = IconRepositoryFactory.repository.by(AppIcon.BOOKMARK)
            try {
                menu!!.removeAll()
                menu!!.add(this.get())
                NotificationFactory.notification.show(NotificationType.INFO, "Gists loaded")
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

    private inner class GistPanel(gists: List<Gist>, gistUrl: String, settings: RocketActionSettings) : JPanel(BorderLayout()) {
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
            buttonUpdate.addActionListener { e: ActionEvent? -> GistWorker(settings).execute() }
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
            textFieldSearch.addActionListener { e: ActionEvent? -> SwingUtilities.invokeLater { fillAndSetModel(textFieldSearch.text) } }
        }
    }

    companion object {
        const val LABEL = "label"
        const val TOKEN = "gistToken"
        const val USERNAME = "username"
        const val BASE_GIST_URL = "baseGistUrl"
    }
}