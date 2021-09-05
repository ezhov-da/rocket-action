package ru.ezhov.rocket.action.types.exec

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JMenuItem
import javax.swing.filechooser.FileSystemView

class ExecRocketActionUi : AbstractRocketAction() {

    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[COMMAND]?.takeIf { it.isNotEmpty() }?.let { command ->
                val workingDir = settings.settings()[WORKING_DIR]?.takeIf { it.isNotEmpty() }
                        ?: File(".").absoluteFile.parent
                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: command
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: command
                val iconUrl = settings.settings()[ICON_URL].orEmpty()

                val menuItem = JMenuItem(label)
                var icon = IconService().load(
                        iconUrl,
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
                menuItem.toolTipText = description
                menuItem.addMouseListener(object : MouseAdapter() {
                    override fun mouseReleased(e: MouseEvent) {
                        if (e.button == MouseEvent.BUTTON3) {
                            val defaultToolkit = Toolkit.getDefaultToolkit()
                            val clipboard = defaultToolkit.systemClipboard
                            clipboard.setContents(StringSelection(command), null)
                            NotificationFactory.notification.show(NotificationType.INFO, "Command '$command' copy to clipboard")
                        } else if (e.button == MouseEvent.BUTTON1) {
                            try {
                                if (workingDir.isEmpty()) {
                                    Runtime.getRuntime().exec(command)
                                } else {
                                    Runtime.getRuntime().exec(
                                            command,
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
                return object : RocketAction {
                    override fun contains(search: String): Boolean =
                            label.contains(search, ignoreCase = true)
                                    .or(command.contains(search, ignoreCase = true))

                    override fun component(): Component = menuItem
                }
            }

    override fun type(): String = "EXEC"

    override fun name(): String = "Выполнить команду"

    override fun description(): String {
        return "description"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(COMMAND, COMMAND, "TEST", true),
                createRocketActionProperty(LABEL, LABEL, "TEST", false),
                createRocketActionProperty(WORKING_DIR, WORKING_DIR, "TEST", false),
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