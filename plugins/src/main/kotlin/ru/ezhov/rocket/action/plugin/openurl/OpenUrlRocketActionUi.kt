package ru.ezhov.rocket.action.plugin.openurl

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
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.Component
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.Icon
import javax.swing.JMenuItem
import javax.swing.event.MenuKeyEvent
import javax.swing.event.MenuKeyListener

class OpenUrlRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private val iconDef = IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT)

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[URL]?.takeIf { it.isNotEmpty() }?.let { url ->
            val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: url
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: url
            val iconUrl = settings.settings()[ICON_URL].orEmpty()
            val menu = JMenuItem(label).apply {
                icon = IconService().load(
                    iconUrl = iconUrl,
                    defaultIcon = iconDef
                )
                toolTipText = description
                isFocusable = true
                addMouseListener(object : MouseAdapter() {
                    override fun mouseReleased(e: MouseEvent) {
                        when (e.button) {
                            MouseEvent.BUTTON1 -> openUrl(url)
                            MouseEvent.BUTTON3 -> copyUrlToClipBoard(url)
                        }
                    }
                })

                addMenuKeyListener(object : MenuKeyListener {
                    override fun menuKeyTyped(e: MenuKeyEvent) = Unit

                    override fun menuKeyPressed(e: MenuKeyEvent) = Unit

                    override fun menuKeyReleased(e: MenuKeyEvent) {
                        when (e.keyCode) {
                            KeyEvent.VK_TAB -> openUrl(url)
                        }
                    }
                })
            }

            object : RocketAction {
                override fun contains(search: String): Boolean =
                    label.contains(search, ignoreCase = true)
                        .or(description.contains(search, ignoreCase = true))

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = menu
            }
        }

    private fun copyUrlToClipBoard(url: String) {
        val defaultToolkit = Toolkit.getDefaultToolkit()
        val clipboard = defaultToolkit.systemClipboard
        clipboard.setContents(StringSelection(url), null)
        NotificationFactory.notification.show(NotificationType.INFO, "URL скопирована в буфер")
    }

    private fun openUrl(url: String) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (ex: Exception) {
                ex.printStackTrace()
                NotificationFactory.notification.show(NotificationType.ERROR, "Ошибка открытия URL")
            }
        }
    }

    override fun type(): RocketActionType = RocketActionType { "OPEN_URL" }

    override fun description(): String = "Открытие ссылки"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL, URL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(LABEL, LABEL.value, "Заголовок", false),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION.value, "Описание", false),
            createRocketActionProperty(URL, URL.value, "URL", true),
            createRocketActionProperty(ICON_URL, ICON_URL.value, "URL иконка", false)
        )
    }

    override fun name(): String = "Открыть ссылку"

    override fun icon(): Icon? = iconDef

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val URL = RocketActionConfigurationPropertyKey("url")
        private val ICON_URL = RocketActionConfigurationPropertyKey("iconUrl")
    }
}
