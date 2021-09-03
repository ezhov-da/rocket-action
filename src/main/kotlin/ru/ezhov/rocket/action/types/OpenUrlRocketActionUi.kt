package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.Component
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.JMenuItem

class OpenUrlRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component {
        val menuItem = JMenuItem(ConfigurationUtil.getValue(settings!!.settings(), LABEL))
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
        return menuItem
    }

    override fun type(): String {
        return "OPEN_URL"
    }

    override fun description(): String {
        return "description"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", true),
                createRocketActionProperty(URL, "TEST", true),
                createRocketActionProperty(ICON_URL, "Icon URL", false)
        )
    }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val URL = "url"
        private const val ICON_URL = "iconUrl"
    }
}