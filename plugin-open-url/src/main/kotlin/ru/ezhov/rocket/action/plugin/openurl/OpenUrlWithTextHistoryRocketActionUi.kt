package ru.ezhov.rocket.action.plugin.openurl

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
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
import javax.swing.Icon
import javax.swing.JMenu
import javax.swing.SwingUtilities

class OpenUrlWithTextHistoryRocketActionUi :
    AbstractRocketAction(), RocketActionPlugin {
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
            val textField = TextFieldWithText(label)
            textField.columns = 10
            textField.toolTipText = description

            val addedToHistory = mutableListOf<String>()
            val action: (text: String) -> Unit =
                { text ->
                    doAction(
                        baseUrl = baseUrl,
                        placeholder = placeholder,
                        text = text,
                        settings = settings,
                        menu = menu,
                        addedToHistory = addedToHistory,
                    )
                }

            textField.addActionListener {
                textField
                    .text
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { t ->
                        doAction(
                            baseUrl = baseUrl,
                            placeholder = placeholder,
                            text = t,
                            settings = settings,
                            menu = menu,
                            addedToHistory = addedToHistory,
                        )
                    }
            }
            menu.add(textField)

            object : RocketAction, RocketActionHandlerFactory {
                override fun contains(search: String): Boolean =
                    label.contains(search, ignoreCase = true)
                        .or(baseUrl.contains(search, ignoreCase = true))
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

                            override fun description(): String = "Открыть URL с подставленным текстом"

                            override fun inputArguments(): List<RocketActionHandlerProperty> = listOf(
                                object : RocketActionHandlerProperty {
                                    override fun key(): RocketActionHandlerPropertyKey =
                                        RocketActionHandlerPropertyKey("text")

                                    override fun name(): String = "Текст для подстановки в URL"

                                    override fun description(): String = "Текст для подстановки в URL"

                                    override fun isRequired(): Boolean = true

                                    override fun property(): RocketActionHandlerPropertySpec =
                                        RocketActionHandlerPropertySpec.StringPropertySpec()
                                }
                            )

                            override fun outputParams(): List<RocketActionHandlerProperty> = emptyList()
                        }
                    )

                    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
                        command.arguments["text"]?.let { text -> action(text) }
                        return RocketActionHandleStatus.Success()
                    }
                }
            }
        }

    private fun doAction(
        baseUrl: String,
        placeholder: String,
        text: String,
        settings: RocketActionSettings,
        menu: JMenu,
        addedToHistory: MutableList<String>
    ) {
        if (Desktop.isDesktopSupported()) {
            try {
                val uri = open(baseUrl = baseUrl, placeholder = placeholder, text = text, settings = settings)
                saveToHistory(text = text, menu = menu, uri = uri, addedToHistory = addedToHistory)
            } catch (ex: Exception) {
                ex.printStackTrace()
                actionContext!!.notification().show(NotificationType.ERROR, "Ошибка открытия URL")
            }
        }
    }

    private fun open(baseUrl: String, placeholder: String, text: String, settings: RocketActionSettings): URI {
        val finalT = if (settings.settings()[IS_ENCODE].toBoolean()) {
            URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        } else {
            text
        }

        val uri = URI(baseUrl.replace(placeholder.toRegex(), finalT))
        Desktop.getDesktop().browse(uri)

        return uri
    }

    private fun saveToHistory(text: String, menu: JMenu, uri: URI, addedToHistory: MutableList<String>) {
        if (!addedToHistory.contains(text)) {
            SwingUtilities.invokeLater {
                OpenUrlRocketActionUi()
                    .create(
                        settings = object : RocketActionSettings {
                            override fun id(): String = text

                            override fun type(): RocketActionType = RocketActionType { "" }

                            override fun settings(): MutableMap<String, String> =
                                mutableMapOf(
                                    "label" to text,
                                    "description" to "Open link",
                                    "url" to uri.toString(),
                                )

                            override fun actions(): List<RocketActionSettings> = emptyList()
                        },
                        context = actionContext!!
                    )
                    ?.component()
                    ?.let { c -> addedToHistory.add(text); menu.add(c) }
                menu.revalidate()
                menu.repaint()
            }
        }
    }

    override fun type(): RocketActionType = RocketActionType { "OPEN_URL_WITH_TEXT_HISTORY" }

    override fun description(): String = "Открыть ссылку с подстановкой и хранением истории открытий ссылок"

    override fun asString(): List<String> = listOf(LABEL, BASE_URL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = BASE_URL, name = BASE_URL, description = "Шаблон URL", required = true),
            createRocketActionProperty(
                key = PLACEHOLDER,
                name = PLACEHOLDER,
                description = "Строка подстановки",
                required = true
            ),
            createRocketActionProperty(key = LABEL, name = LABEL, description = "Заголовок", required = false),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = DESCRIPTION,
                description = "Описание",
                required = false
            ),
            createRocketActionProperty(key = ICON_URL, name = ICON_URL, description = "URL иконки", required = false),
            createRocketActionProperty(
                key = IS_ENCODE,
                name = IS_ENCODE,
                description = "Кодировать для URL",
                required = false,
                property = RocketActionPropertySpec.BooleanPropertySpec(defaultValue = false),
            )
        )
    }

    override fun name(): String = "Открытие ссылки с подстановкой и с сохранением истории"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.LINK_INTACT)

    companion object {
        private val LABEL = "label"
        private val IS_ENCODE = "isEncode"
        private val DESCRIPTION = "description"
        private val BASE_URL = "baseUrl"
        private val ICON_URL = "iconUrl"
        private val PLACEHOLDER = "placeholder"
    }
}
