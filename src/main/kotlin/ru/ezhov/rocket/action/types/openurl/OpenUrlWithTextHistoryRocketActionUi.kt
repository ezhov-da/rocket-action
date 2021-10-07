package ru.ezhov.rocket.action.types.openurl

import ru.ezhov.rocket.action.api.PropertyType
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.Component
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.swing.JMenu
import javax.swing.SwingUtilities

class OpenUrlWithTextHistoryRocketActionUi : AbstractRocketAction() {
    private var label: String? = null

    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[BASE_URL]?.takeIf { it.isNotEmpty() }?.let { baseUrl ->
                val placeholder = settings.settings()[PLACEHOLDER].orEmpty()
                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: baseUrl
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: baseUrl
                val iconUrl = settings.settings()[ICON_URL].orEmpty()

                val menu = JMenu(label)
                menu.icon = IconService().load(
                        iconUrl,
                        IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT)
                )
                val textField = TextFieldWithText(label)
                textField.columns = 10
                textField.toolTipText = description

                val addedToHistory = mutableListOf<String>()

                textField.addActionListener {
                    textField
                            .text
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { t ->
                                if (Desktop.isDesktopSupported()) {
                                    try {
                                        val finalT = if (settings.settings()[IS_ENCODE].toBoolean())
                                            URLEncoder.encode(t, StandardCharsets.UTF_8.toString())
                                        else t

                                        val uri = URI(baseUrl.replace(placeholder.toRegex(), finalT))
                                        Desktop.getDesktop().browse(uri)
                                        if (!addedToHistory.contains(t)) {
                                            SwingUtilities.invokeLater {
                                                OpenUrlRocketActionUi()
                                                        .create(object : RocketActionSettings {
                                                            override fun id(): String = t

                                                            override fun type(): RocketActionType = RocketActionType { "" }

                                                            override fun settings(): MutableMap<RocketActionConfigurationPropertyKey, String> = mutableMapOf(
                                                                    RocketActionConfigurationPropertyKey("label") to t,
                                                                    RocketActionConfigurationPropertyKey("description") to "Open link",
                                                                    RocketActionConfigurationPropertyKey("url") to uri.toString(),
                                                            )

                                                            override fun actions(): List<RocketActionSettings> = emptyList()
                                                        })
                                                        ?.component()
                                                        ?.let { c -> addedToHistory.add(t); menu.add(c) }
                                                menu.revalidate()
                                                menu.repaint()
                                            }
                                        }
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                        NotificationFactory.notification.show(NotificationType.ERROR, "Ошибка открытия URL")
                                    }
                                }
                            }
                }
                menu.add(textField)

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                            label.contains(search, ignoreCase = true)
                                    .or(baseUrl.contains(search, ignoreCase = true))
                                    .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                            !(settings.id() == actionSettings.id() &&
                                    settings.settings() == actionSettings.settings())

                    override fun component(): Component = menu
                }
            }

    override fun type(): RocketActionType = RocketActionType { "OPEN_URL_WITH_TEXT_HISTORY" }

    override fun description(): String = "Открыть ссылку с подстановкой и хранением истории открытий ссылок"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL, BASE_URL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(BASE_URL, BASE_URL.value, "Шаблон URL", true),
                createRocketActionProperty(PLACEHOLDER, PLACEHOLDER.value, "Строка подстановки", true),
                createRocketActionProperty(LABEL, LABEL.value, "Заголовок", false),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION.value, "Описание", false),
                createRocketActionProperty(ICON_URL, ICON_URL.value, "URL иконки", false),
                createRocketActionProperty(
                        IS_ENCODE,
                        IS_ENCODE.value,
                        "Кодировать для URL",
                        false,
                        default = "false",
                        type = PropertyType.BOOLEAN
                )
        )
    }

    override fun name(): String = "Открытие ссылки с подстановкой и с сохранением истории"

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val IS_ENCODE = RocketActionConfigurationPropertyKey("isEncode")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val BASE_URL = RocketActionConfigurationPropertyKey("baseUrl")
        private val ICON_URL = RocketActionConfigurationPropertyKey("iconUrl")
        private val PLACEHOLDER = RocketActionConfigurationPropertyKey("placeholder")
    }
}