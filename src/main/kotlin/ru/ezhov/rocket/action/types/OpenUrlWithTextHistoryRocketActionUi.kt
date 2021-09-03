package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.Component
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.net.URI
import javax.swing.JMenu
import javax.swing.SwingUtilities

class OpenUrlWithTextHistoryRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component {
        val menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menu.icon = IconService().load(
                settings.settings()[ICON_URL].orEmpty(),
                IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT)
        )
        val textField = TextFieldWithText(ConfigurationUtil.getValue(settings.settings(), LABEL))
        textField.columns = 10
        textField.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        textField.addActionListener { e: ActionEvent? ->
            textField
                    .text
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { t ->
                        if (Desktop.isDesktopSupported()) {
                            try {
                                val uri = URI(
                                        ConfigurationUtil.getValue(settings.settings(), BASE_URL)
                                                .replace(ConfigurationUtil.getValue(settings.settings(), PLACEHOLDER).toRegex(), t)
                                )
                                Desktop.getDesktop().browse(uri)
                                SwingUtilities.invokeLater {
                                    val map: MutableMap<String, String> = HashMap()
                                    map["label"] = t
                                    map["description"] = "Open link"
                                    map["url"] = uri.toString()
                                    menu.add(OpenUrlRocketActionUi().create(object : RocketActionSettings {
                                        override fun id(): String = ""

                                        override fun type(): String = ""

                                        override fun settings(): MutableMap<String, String> = mutableMapOf()

                                        override fun actions(): List<RocketActionSettings> = emptyList()
                                    }))
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
        return menu
    }

    override fun type(): String {
        return "OPEN_URL_WITH_TEXT_HISTORY"
    }

    override fun description(): String {
        return "description"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", true),
                createRocketActionProperty(BASE_URL, "TEST", true),
                createRocketActionProperty(PLACEHOLDER, "TEST", true),
                createRocketActionProperty(ICON_URL, "Icon URL", false)
        )
    }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val BASE_URL = "baseUrl"
        private const val ICON_URL = "iconUrl"
        private const val PLACEHOLDER = "placeholder"
    }
}