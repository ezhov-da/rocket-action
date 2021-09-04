package ru.ezhov.rocket.action.types.openfile

import ru.ezhov.rocket.action.api.Action
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.SearchableAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.types.ConfigurationUtil
import java.awt.Component
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.JMenuItem

class OpenFileRocketActionUi : AbstractRocketAction() {

    override fun create(settings: RocketActionSettings): Action {
        val label = ConfigurationUtil.getValue(settings.settings(), LABEL)
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
        return object : Action {
            override fun action(): SearchableAction = object : SearchableAction {
                override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
            }

            override fun component(): Component = menuItem
        }
    }

    override fun type(): String = "OPEN_FILE"

    override fun description(): String = "description"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", true),
                createRocketActionProperty(PATH, PATH, "TEST", true)
        )
    }

    override fun name(): String = "Открыть файл"

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val PATH = "path"
    }
}