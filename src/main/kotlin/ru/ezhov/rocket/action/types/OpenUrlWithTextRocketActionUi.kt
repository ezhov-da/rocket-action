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
import java.net.URI
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel

class OpenUrlWithTextRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component {
        val menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menu.icon = IconService().load(
                settings.settings()[ICON_URL].orEmpty(),
                IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT)
        )
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        panel.add(
                JLabel(IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT))
        )
        val textField = TextFieldWithText(ConfigurationUtil.getValue(settings.settings(), LABEL))
        textField.columns = 10
        panel.add(textField)
        textField.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        textField.addActionListener {
            textField
                    .text
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { t ->
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(
                                        URI(
                                                ConfigurationUtil.getValue(settings.settings(), BASE_URL).replace(
                                                        ConfigurationUtil.getValue(settings.settings(), PLACEHOLDER).toRegex(), t
                                                )
                                        )
                                )
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
        return "OPEN_URL_WITH_TEXT"
    }

    override fun description(): String {
        return "description"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", true),
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