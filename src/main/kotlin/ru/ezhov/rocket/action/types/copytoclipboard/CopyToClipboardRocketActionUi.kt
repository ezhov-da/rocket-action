package ru.ezhov.rocket.action.types.copytoclipboard

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JMenuItem

class CopyToClipboardRocketActionUi : AbstractRocketAction() {

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val TEXT = "text"
    }

    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[TEXT]?.takeIf { it.isNotEmpty() }?.let { text ->
                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: text
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: text
                val menuItem = JMenuItem(label)
                menuItem.icon = IconRepositoryFactory.repository.by(AppIcon.CLIPBOARD)
                menuItem.toolTipText = description
                menuItem.addActionListener {
                    val defaultToolkit = Toolkit.getDefaultToolkit()
                    val clipboard = defaultToolkit.systemClipboard
                    clipboard.setContents(StringSelection(text), null)
                    NotificationFactory.notification.show(NotificationType.INFO, "Текст скопирован в буфер")
                }

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                            text.contains(search, ignoreCase = true)
                                    .or(label.contains(search, ignoreCase = true))
                                    .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                            !(settings.id() == actionSettings.id() &&
                                    settings.settings() == actionSettings.settings())

                    override fun component(): Component = menuItem
                }
            }

    override fun type(): RocketActionType = RocketActionType { "COPY_TO_CLIPBOARD" }

    override fun description(): String {
        return "Позволяет скопировать текст в буфер"
    }

    override fun name(): String = "Копировать в буфер"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "Текст для отображения", false),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "Описание", false),
                createRocketActionProperty(TEXT, TEXT, "Текст для копирования в буфер", true)
        )
    }


}