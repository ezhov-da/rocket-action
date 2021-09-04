package ru.ezhov.rocket.action.types.openurl

import ru.ezhov.rocket.action.api.Action
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.SearchableAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.types.ConfigurationUtil
import java.awt.Component
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.JMenuItem

class OpenUrlRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Action {
        val label = ConfigurationUtil.getValue(settings.settings(), LABEL)
        val menuItem = JMenuItem(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menuItem.icon = IconService().load(
                settings.settings()[ICON_URL].orEmpty(),
                IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT)
        )
        menuItem.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        menuItem.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON3) {
                    val defaultToolkit = Toolkit.getDefaultToolkit()
                    val clipboard = defaultToolkit.systemClipboard
                    clipboard.setContents(StringSelection(ConfigurationUtil.getValue(settings.settings(), URL)), null)
                    NotificationFactory.notification.show(NotificationType.INFO, "URL copy to clipboard")
                } else if (e.button == MouseEvent.BUTTON1) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(URI(ConfigurationUtil.getValue(settings.settings(), URL)))
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            NotificationFactory.notification.show(NotificationType.ERROR, "Error open url")
                        }
                    }
                }
            }
        })
        return object : Action {
            override fun action(): SearchableAction = object : SearchableAction {
                override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
            }

            override fun component(): Component = menuItem
        }
    }

    override fun type(): String = "OPEN_URL"

    override fun description(): String = "description"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", true),
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