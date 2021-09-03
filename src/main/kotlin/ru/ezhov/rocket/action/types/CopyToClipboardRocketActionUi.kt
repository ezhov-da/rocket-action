package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import javax.swing.JMenuItem

class CopyToClipboardRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component {
        val menuItem = JMenuItem(ConfigurationUtil.getValue(settings!!.settings(), LABEL))
        menuItem.icon = IconRepositoryFactory.repository.by(AppIcon.CLIPBOARD)
        menuItem.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        menuItem.addActionListener { e: ActionEvent? ->
            val defaultToolkit = Toolkit.getDefaultToolkit()
            val clipboard = defaultToolkit.systemClipboard
            clipboard.setContents(StringSelection(ConfigurationUtil.getValue(settings.settings(), TEXT)), null)
            NotificationFactory.notification.show(NotificationType.INFO, "Text copy to clipboard")
        }
        return menuItem
    }

    override fun type(): String {
        return "COPY_TO_CLIPBOARD"
    }

    override fun description(): String {
        return "Allows you to copy a previously prepared text to the clipboard"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, "Displayed title", true),
                createRocketActionProperty(DESCRIPTION, "Description that will be displayed as a hint", true),
                createRocketActionProperty(TEXT, "Text prepared for copying to the clipboard", true)
        )
    }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val TEXT = "text"
    }
}