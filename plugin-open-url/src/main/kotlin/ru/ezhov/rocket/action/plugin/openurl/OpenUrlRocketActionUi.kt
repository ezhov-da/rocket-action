package ru.ezhov.rocket.action.plugin.openurl

import mu.KotlinLogging
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
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.config.ConfigReaderFactory
import java.awt.Component
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import java.util.*
import javax.swing.Icon
import javax.swing.JMenuItem
import javax.swing.event.MenuKeyEvent
import javax.swing.event.MenuKeyListener

private val logger = KotlinLogging.logger {}

class OpenUrlRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null
    private val reader =
        OpenUrlRocketActionUi::class.java.getResourceAsStream("/openurlrocketactionui/config.yml").use {
            ConfigReaderFactory.yml(it!!)
        }

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-open-url.properties"))
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

            context.search().register(settings.id(), label)
            context.search().register(settings.id(), description)

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

                            override fun description(): String = "Open URL"

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
        actionContext!!.notification().show(NotificationType.INFO, "URL copied to clipboard")
    }

    private fun openUrl(url: String) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (ex: Exception) {
                logger.warn(ex) { "Error when open url by '$url'" }

                ex.printStackTrace()
                actionContext!!.notification().show(NotificationType.ERROR, "URL opening error")
            }
        }
    }

    override fun type(): RocketActionType = RocketActionType { "OPEN_URL" }

    override fun description(): String = reader.description()

    override fun asString(): List<String> = listOf(LABEL, URL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = reader.nameBy(LABEL),
                description = reader.descriptionBy(LABEL),
                required = false
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = reader.nameBy(DESCRIPTION),
                description = reader.descriptionBy(DESCRIPTION),
                required = false
            ),
            createRocketActionProperty(
                key = URL,
                name = reader.nameBy(URL),
                description = reader.descriptionBy(URL),
                required = true
            ),
            createRocketActionProperty(
                key = ICON_URL,
                name = reader.nameBy(ICON_URL),
                description = reader.descriptionBy(ICON_URL),
                required = false
            )
        )
    }

    override fun name(): String = reader.name()

    override fun icon(): Icon = actionContext!!.icon().by(AppIcon.LINK_INTACT)

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val URL = "url"
        private const val ICON_URL = "iconUrl"
    }
}
