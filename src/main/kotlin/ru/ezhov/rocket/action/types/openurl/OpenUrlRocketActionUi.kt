package ru.ezhov.rocket.action.types.openurl

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import java.awt.Component
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.JMenuItem

class OpenUrlRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[URL]?.takeIf { it.isNotEmpty() }?.let { url ->
                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: url
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: url
                val iconUrl = settings.settings()[ICON_URL].orEmpty()

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                            label.contains(search, ignoreCase = true)
                                    .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                            !(settings.id() == actionSettings.id() &&
                                    settings.settings() == actionSettings.settings())

                    override fun component(): Component = JMenuItem(label).apply {
                        icon = IconService().load(
                                iconUrl,
                                IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT)
                        )
                        toolTipText = description
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseReleased(e: MouseEvent) {
                                when (e.button) {
                                    MouseEvent.BUTTON3 -> {
                                        val defaultToolkit = Toolkit.getDefaultToolkit()
                                        val clipboard = defaultToolkit.systemClipboard
                                        clipboard.setContents(StringSelection(url), null)
                                        NotificationFactory.notification.show(NotificationType.INFO, "URL copy to clipboard")
                                    }
                                    MouseEvent.BUTTON1 -> {
                                        if (Desktop.isDesktopSupported()) {
                                            try {
                                                Desktop.getDesktop().browse(URI(url))
                                            } catch (ex: Exception) {
                                                ex.printStackTrace()
                                                NotificationFactory.notification.show(NotificationType.ERROR, "Error open url")
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }


    override fun type(): String = "OPEN_URL"

    override fun description(): String = "description"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "TEST", false),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", false),
                createRocketActionProperty(URL, URL, "TEST", true),
                createRocketActionProperty(ICON_URL, ICON_URL, "Icon URL", false)
        )
    }

    override fun name(): String = "Открыть ссылку"

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val URL = "url"
        private const val ICON_URL = "iconUrl"
    }
}