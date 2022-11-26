package ru.ezhov.rocket.action.plugin.openurl

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
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
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
    private var actionContext: RocketActionContext? = null

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[URL]?.takeIf { it.isNotEmpty() }?.let { url ->
            val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: url
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: url
            val iconUrl = settings.settings()[ICON_URL].orEmpty()
            val menu = JMenuItem(label).apply {
                icon = context.icon().load(
                    iconUrl = iconUrl,
                    defaultIcon = context.icon().by(AppIcon.LINK_INTACT)
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

            object : RocketAction, RocketActionHandlerFactory {
                override fun contains(search: String): Boolean =
                    label.contains(search, ignoreCase = true)
                        .or(description.contains(search, ignoreCase = true))

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = menu

                override fun handler(): RocketActionHandler = object : RocketActionHandler {
                    override fun id(): String = settings.id()

                    override fun contracts(): List<RocketActionHandlerCommandContract> = listOf(
                        object : RocketActionHandlerCommandContract {
                            override fun commandName(): String = "openUrl"

                            override fun title(): String = label

                            override fun description(): String = "Открыть URL"

                            override fun inputArguments(): List<RocketActionHandlerProperty> = emptyList()

                            override fun outputParams(): List<RocketActionHandlerProperty> = emptyList()
                        }
                    )

                    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
                        openUrl(url)
                        return RocketActionHandleStatus.Success()
                    }
                }
            }
        }

    private fun copyUrlToClipBoard(url: String) {
        val defaultToolkit = Toolkit.getDefaultToolkit()
        val clipboard = defaultToolkit.systemClipboard
        clipboard.setContents(StringSelection(url), null)
        actionContext!!.notification().show(NotificationType.INFO, "URL скопирована в буфер")
    }

    private fun openUrl(url: String) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (ex: Exception) {
                ex.printStackTrace()
                actionContext!!.notification().show(NotificationType.ERROR, "Ошибка открытия URL")
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

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.LINK_INTACT)

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val URL = RocketActionConfigurationPropertyKey("url")
        private val ICON_URL = RocketActionConfigurationPropertyKey("iconUrl")
    }
}
