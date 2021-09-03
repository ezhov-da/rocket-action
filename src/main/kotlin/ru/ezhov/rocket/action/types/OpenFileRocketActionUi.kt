package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.Component
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.JMenuItem

class OpenFileRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component {
        val menuItem = JMenuItem(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menuItem.icon = IconRepositoryFactory.repository.by(AppIcon.FILE)
        menuItem.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        menuItem.addActionListener { e: ActionEvent? ->
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(File(ConfigurationUtil.getValue(settings.settings(), PATH)))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    NotificationFactory.notification.show(NotificationType.ERROR, "Error open file")
                }
            }
        }
        return menuItem
    }

    override fun type(): String {
        return "OPEN_FILE"
    }

    override fun description(): String {
        return "description"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", true),
                createRocketActionProperty(PATH, "TEST", true)
        )
    }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val PATH = "path"
    }
}