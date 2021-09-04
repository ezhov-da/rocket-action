package ru.ezhov.rocket.action.types.exec

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.types.ConfigurationUtil
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JMenuItem
import javax.swing.filechooser.FileSystemView

class ExecRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component {
        val menuItem = JMenuItem(ConfigurationUtil.getValue(settings.settings(), LABEL))
        val command = ConfigurationUtil.getValue(settings.settings(), COMMAND)
        var icon = IconService().load(
                settings.settings()[ICON_URL].orEmpty(),
                IconRepositoryFactory.repository.by(AppIcon.FIRE)
        )
        try {
            val file = File(command)
            if (file.exists()) {
                icon = FileSystemView.getFileSystemView().getSystemIcon(file)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        menuItem.icon = icon
        menuItem.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        menuItem.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON3) {
                    val defaultToolkit = Toolkit.getDefaultToolkit()
                    val clipboard = defaultToolkit.systemClipboard
                    clipboard.setContents(StringSelection(ConfigurationUtil.getValue(settings.settings(), COMMAND)), null)
                    NotificationFactory.notification.show(NotificationType.INFO, "Command '$command' copy to clipboard")
                } else if (e.button == MouseEvent.BUTTON1) {
                    try {
                        val workingDir = ConfigurationUtil.getValue(settings.settings(), WORKING_DIR)
                        if ("" == workingDir) {
                            Runtime.getRuntime().exec(ConfigurationUtil.getValue(settings.settings(), COMMAND))
                        } else {
                            Runtime.getRuntime().exec(
                                    ConfigurationUtil.getValue(settings.settings(), COMMAND),
                                    null,
                                    File(workingDir)
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        })
        return menuItem
    }

    override fun type(): String {
        return "EXEC"
    }

    override fun name(): String = "Выполнить команду"

    override fun description(): String {
        return "description"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "TEST", true),
                createRocketActionProperty(COMMAND, COMMAND, "TEST", true),
                createRocketActionProperty(WORKING_DIR, WORKING_DIR, "TEST", true),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", false),
                createRocketActionProperty(ICON_URL, ICON_URL, "Icon URL", false)
        )
    }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val COMMAND = "command"
        private const val WORKING_DIR = "workingDirectory"
        private const val ICON_URL = "iconUrl"
    }
}