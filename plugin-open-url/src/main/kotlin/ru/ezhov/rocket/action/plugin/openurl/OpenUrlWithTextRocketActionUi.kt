package ru.ezhov.rocket.action.plugin.openurl

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
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
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertyKey
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertySpec
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.Component
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel

class OpenUrlWithTextRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

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
        settings.settings()[BASE_URL]?.takeIf { it.isNotEmpty() }?.let { baseUrl ->
            val placeholder = settings.settings()[PLACEHOLDER].orEmpty()
            val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: baseUrl
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: baseUrl
            val iconUrl = settings.settings()[ICON_URL].orEmpty()

            val menu = JMenu(label)
            menu.icon = context.icon().load(
                iconUrl = iconUrl,
                defaultIcon = actionContext!!.icon().by(AppIcon.LINK_INTACT)
            )
            val panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
            panel.add(JLabel(actionContext!!.icon().by(AppIcon.LINK_INTACT)))
            val textField = TextFieldWithText(label)
            textField.columns = 10
            panel.add(textField)
            textField.toolTipText = description
            val action: (text: String) -> Unit = { text ->
                openUrl(
                    baseUrl = baseUrl,
                    placeholder = placeholder,
                    text = text,
                    settings = settings
                )
            }
            textField.addActionListener {
                textField
                    .text
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { t -> action(t) }
            }
            menu.add(textField)

            context.search().register(settings.id(), label)

            object : RocketAction, RocketActionHandlerFactory {
                override fun contains(search: String): Boolean =
                    label.contains(search, ignoreCase = true)

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = menu

                override fun handler(): RocketActionHandler = object : RocketActionHandler {
                    override fun id(): String = settings.id()

                    override fun contracts(): List<RocketActionHandlerCommandContract> =
                        listOf(
                            object : RocketActionHandlerCommandContract {
                                override fun commandName(): String = "openUrl"

                                override fun title(): String = label

                                override fun description(): String = "Open URL with wildcard"

                                override fun inputArguments(): List<RocketActionHandlerProperty> =
                                    listOf(
                                        object : RocketActionHandlerProperty {
                                            override fun key(): RocketActionHandlerPropertyKey =
                                                RocketActionHandlerPropertyKey("text")

                                            override fun name(): String = "Substitute text"

                                            override fun description(): String = "Substitute text"

                                            override fun isRequired(): Boolean = true

                                            override fun property(): RocketActionHandlerPropertySpec =
                                                RocketActionHandlerPropertySpec.StringPropertySpec()
                                        }
                                    )

                                override fun outputParams(): List<RocketActionHandlerProperty> = emptyList()
                            }
                        )

                    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
                        command.arguments["text"]?.let { text ->
                            action(text)
                        }

                        return RocketActionHandleStatus.Success()
                    }
                }
            }
        }

    private fun openUrl(baseUrl: String, placeholder: String, text: String, settings: RocketActionSettings) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(
                    URI(
                        baseUrl.replace(
                            placeholder.toRegex(),
                            if (settings.settings()[IS_ENCODE].toBoolean())
                                URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
                            else text
                        )
                    )
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                actionContext!!.notification().show(NotificationType.ERROR, "URL opening error")
            }
        }
    }

    override fun type(): RocketActionType = RocketActionType { "OPEN_URL_WITH_TEXT" }

    override fun description(): String = "Opening a template link with a value substituted"

    override fun asString(): List<String> = listOf(LABEL, BASE_URL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(LABEL, LABEL, "Title", false),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION, "Description", false),
            createRocketActionProperty(BASE_URL, BASE_URL, "URL pattern", true),
            createRocketActionProperty(PLACEHOLDER, PLACEHOLDER, "Substitution string", true),
            createRocketActionProperty(ICON_URL, ICON_URL, "Icon URL", false),
            createRocketActionProperty(
                key = IS_ENCODE,
                name = IS_ENCODE,
                description = "Encode for URL",
                required = false,
                property = RocketActionPropertySpec.BooleanPropertySpec(defaultValue = false),
            )
        )
    }

    override fun name(): String = "Open link with wildcard"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.LINK_INTACT)

    companion object {
        private val ICON_URL = "iconUrl"
        private val LABEL = "label"
        private val IS_ENCODE = "isEncode"
        private val DESCRIPTION = "description"
        private val BASE_URL = "baseUrl"
        private val PLACEHOLDER = "placeholder"
    }
}
