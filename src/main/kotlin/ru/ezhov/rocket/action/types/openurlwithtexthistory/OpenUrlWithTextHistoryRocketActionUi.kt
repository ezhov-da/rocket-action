package ru.ezhov.rocket.action.types.openurlwithtexthistory

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
import ru.ezhov.rocket.action.types.openurl.OpenUrlRocketActionUi
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.Component
import java.awt.Desktop
import java.net.URI
import javax.swing.JMenu
import javax.swing.SwingUtilities

class OpenUrlWithTextHistoryRocketActionUi : AbstractRocketAction() {
    private var label: String? = null

    override fun create(settings: RocketActionSettings): Action? =
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
                textField.addActionListener {
                    textField
                            .text
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { t ->
                                if (Desktop.isDesktopSupported()) {
                                    try {
                                        val uri = URI(
                                                baseUrl.replace(placeholder.toRegex(), t)
                                        )
                                        Desktop.getDesktop().browse(uri)
                                        SwingUtilities.invokeLater {
                                            val map: MutableMap<String, String> = HashMap()
                                            map["label"] = t
                                            map["description"] = "Open link"
                                            map["url"] = uri.toString()
                                            OpenUrlRocketActionUi().create(object : RocketActionSettings {
                                                override fun id(): String = ""

                                                override fun type(): String = ""

                                                override fun settings(): MutableMap<String, String> = mutableMapOf()

                                                override fun actions(): List<RocketActionSettings> = emptyList()
                                            })?.component()?.let { c -> menu.add(c) }

                                            menu.revalidate()
                                            menu.repaint()
                                        }
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                        NotificationFactory.notification.show(NotificationType.ERROR, "Error open URL")
                                    }
                                }
                            }
                }
                menu.add(textField)

                object : Action {
                    override fun action(): SearchableAction = object : SearchableAction {
                        override fun contains(search: String): Boolean =
                                label.contains(search, ignoreCase = true)
                                        .or(baseUrl.contains(search, ignoreCase = true))
                                        .or(description.contains(search, ignoreCase = true))
                    }

                    override fun component(): Component = menu
                }
            }

    override fun type(): String = "OPEN_URL_WITH_TEXT_HISTORY"

    override fun description(): String {
        return "description"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(BASE_URL, BASE_URL, "TEST", true),
                createRocketActionProperty(PLACEHOLDER, PLACEHOLDER, "TEST", true),
                createRocketActionProperty(LABEL, LABEL, "TEST", false),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", false),
                createRocketActionProperty(ICON_URL, ICON_URL, "Icon URL", false)
        )
    }

    override fun name(): String = "Открытие ссылки с подстановкой и с сохранением истории"

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val BASE_URL = "baseUrl"
        private const val ICON_URL = "iconUrl"
        private const val PLACEHOLDER = "placeholder"
    }
}