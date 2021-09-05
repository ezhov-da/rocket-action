package ru.ezhov.rocket.action.types.openurlwithtext

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
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
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel

class OpenUrlWithTextRocketActionUi : AbstractRocketAction() {

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
                val panel = JPanel()
                panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
                panel.add(
                        JLabel(IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT))
                )
                val textField = TextFieldWithText(label)
                textField.columns = 10
                panel.add(textField)
                textField.toolTipText = description
                textField.addActionListener {
                    textField
                            .text
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { t ->
                                if (Desktop.isDesktopSupported()) {
                                    try {
                                        Desktop.getDesktop().browse(
                                                URI(baseUrl.replace(placeholder.toRegex(), t))
                                        )
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                        NotificationFactory.notification.show(NotificationType.ERROR, "Error open URL")
                                    }
                                }
                            }
                }
                menu.add(textField)

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                            label.contains(search, ignoreCase = true)

                    override fun component(): Component = menu
                }
            }

    override fun type(): String = "OPEN_URL_WITH_TEXT"

    override fun description(): String {
        return "description"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "TEST", false),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", false),
                createRocketActionProperty(BASE_URL, BASE_URL, "TEST", true),
                createRocketActionProperty(PLACEHOLDER, PLACEHOLDER, "TEST", true),
                createRocketActionProperty(ICON_URL, ICON_URL, "Icon URL", false)
        )
    }

    override fun name(): String = "Открыть ссылку с подстановкой"

    companion object {
        private const val ICON_URL = "iconUrl"
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val BASE_URL = "baseUrl"
        private const val PLACEHOLDER = "placeholder"
    }
}