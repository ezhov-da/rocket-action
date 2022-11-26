package ru.ezhov.rocket.action.plugin.exec

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.Icon
import javax.swing.JMenuItem
import javax.swing.filechooser.FileSystemView

private val logger = KotlinLogging.logger { }

class ExecRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null
    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[COMMAND]?.takeIf { it.isNotEmpty() }?.let { command ->
            actionContext = context

            val workingDir = settings.settings()[WORKING_DIR]?.takeIf { it.isNotEmpty() }
                ?: File(".").absoluteFile.parent
            val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: command
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: command
            val iconUrl = settings.settings()[ICON_URL].orEmpty()
            val menuIcon = icon(iconUrl = iconUrl, command = command, iconService = context.icon())
            val menu = JMenuItem(label).apply {
                icon = menuIcon
                toolTipText = description
                addActionListener { executeCommand(workingDir, command, notification = context.notification()) }
                addMouseListener(object : MouseAdapter() {
                    override fun mouseReleased(e: MouseEvent) {
                        if (e.button == MouseEvent.BUTTON3) {
                            copyToClipboard(command = command, notification = context.notification())
                        }
                    }
                })
            }

            object : RocketAction {
                override fun contains(search: String): Boolean =
                    label.contains(search, ignoreCase = true)
                        .or(command.contains(search, ignoreCase = true))

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = menu
            }
        }

    private fun icon(iconUrl: String, command: String, iconService: IconService): Icon {
        var menuIcon = iconService.load(
            iconUrl = iconUrl,
            defaultIcon = actionContext!!.icon().by(AppIcon.FIRE)
        )
        try {
            val file = File(command)
            if (file.exists()) {
                menuIcon = FileSystemView.getFileSystemView().getSystemIcon(file)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return menuIcon
    }

    private fun copyToClipboard(command: String, notification: NotificationService) {
        val defaultToolkit = Toolkit.getDefaultToolkit()
        val clipboard = defaultToolkit.systemClipboard
        clipboard.setContents(StringSelection(command), null)
        notification.show(NotificationType.INFO, "Команда '$command' скопирована в буфер")
    }

    private fun executeCommand(workingDir: String, command: String, notification: NotificationService) {
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
            val text = "Ошибка выполнения команды '$command'"
            logger.warn(ex) { text }
            notification.show(NotificationType.WARN, text)
        }
    }

    override fun type(): RocketActionType = RocketActionType { "EXEC" }

    override fun name(): String = "Выполнить команду"

    override fun description(): String = "Выполнение команды"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(
        LABEL, COMMAND
    )

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(COMMAND, COMMAND.value, "Команда", true),
            createRocketActionProperty(LABEL, LABEL.value, "Заголовок", false),
            createRocketActionProperty(WORKING_DIR, WORKING_DIR.value, "Рабочий каталог", false),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION.value, "Описание", false),
            createRocketActionProperty(ICON_URL, ICON_URL.value, "URL иконки", false)
        )
    }

    override fun icon(): Icon = actionContext!!.icon().by(AppIcon.FIRE)

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val COMMAND = RocketActionConfigurationPropertyKey("command")
        private val WORKING_DIR = RocketActionConfigurationPropertyKey("workingDirectory")
        private val ICON_URL = RocketActionConfigurationPropertyKey("iconUrl")
    }
}
