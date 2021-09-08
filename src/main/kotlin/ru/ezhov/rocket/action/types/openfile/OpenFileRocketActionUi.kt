package ru.ezhov.rocket.action.types.openfile

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import java.awt.Component
import java.awt.Desktop
import java.io.File
import javax.swing.JMenuItem

class OpenFileRocketActionUi : AbstractRocketAction() {

    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[PATH]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { path ->
                        val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }
                                ?: path.let { File(path).name }
                        val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: path

                        val menuItem = JMenuItem(label)
                        menuItem.icon = IconRepositoryFactory.repository.by(AppIcon.FILE)
                        menuItem.toolTipText = description
                        menuItem.addActionListener {
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().open(File(path))
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                    NotificationFactory.notification.show(NotificationType.ERROR, "Error open file")
                                }
                            }
                        }

                        object : RocketAction {
                            override fun contains(search: String): Boolean =
                                    path.contains(search, ignoreCase = true)
                                            .or(label.contains(search, ignoreCase = true))
                                            .or(description.contains(search, ignoreCase = true))

                            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                    !(settings.id() == actionSettings.id() &&
                                            settings.settings() == actionSettings.settings())

                            override fun component(): Component = menuItem
                        }
                    }

    override fun type(): String = "OPEN_FILE"

    override fun description(): String = "description"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(
                        key = LABEL,
                        name = "Заголовок",
                        description =
                        """Заголовок, который будет отображаться. 
                            |В случае отсутствия будет использоваться имя файла
                        """.trimMargin(),
                        required = false
                ),
                createRocketActionProperty(
                        key = DESCRIPTION,
                        name = "Описание",
                        description = """Описание, которое будет всплывать при наведении, 
                            |в случае отсутствия будет отображаться путь""".trimMargin(),
                        required = false
                ),
                createRocketActionProperty(
                        key = PATH,
                        name = "Путь к файлу",
                        description = "Путь по которому будет открываться файл",
                        required = true
                )
        )
    }

    override fun name(): String = "Открыть файл"

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val PATH = "path"
    }
}