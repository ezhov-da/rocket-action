package ru.ezhov.rocket.action.plugin.text

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JTextPane

class TextRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private val icon = IconRepositoryFactory.repository.by(AppIcon.TEXT)

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun description(): String = "Show text"

    override fun properties(): List<RocketActionConfigurationProperty> =
        listOf(
            createRocketActionProperty(key = LABEL, name = LABEL.value, description = "Текст для отображения", required = true),
            createRocketActionProperty(key = DESCRIPTION, name = DESCRIPTION.value, description = "Описание", required = false)
        )

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
            val description = settings.settings()[DESCRIPTION]
            val textPane = JTextPane().apply {
                text = label
                description?.let { description ->
                    this.toolTipText = description
                }
                isEditable = false
                background = JLabel().background
                addMouseListener(object : MouseAdapter() {
                    override fun mouseReleased(e: MouseEvent) {
                        if (e.button == MouseEvent.BUTTON3) {
                            val defaultToolkit = Toolkit.getDefaultToolkit()
                            val clipboard = defaultToolkit.systemClipboard
                            clipboard.setContents(StringSelection(text), null)
                            NotificationFactory.notification.show(NotificationType.INFO, "Текст скопирован в буфер")
                        }
                    }
                })
            }
            object : RocketAction {
                override fun contains(search: String): Boolean =
                    label.contains(search, ignoreCase = true)
                        .or(description?.contains(search, ignoreCase = true) ?: false)

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = textPane
            }
        }

    override fun type(): RocketActionType = RocketActionType { "SHOW_TEXT" }

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL)

    override fun name(): String = "Отобразить текст"

    override fun icon(): Icon? = icon

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
    }

}
