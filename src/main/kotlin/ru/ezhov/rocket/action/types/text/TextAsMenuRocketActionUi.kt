package ru.ezhov.rocket.action.types.text

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
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JTextPane

class TextAsMenuRocketActionUi : AbstractRocketAction() {
    override fun description(): String = "Show text"

    override fun properties(): List<RocketActionConfigurationProperty> =
            listOf(
                    createRocketActionProperty(LABEL, LABEL, "Заголовок", true),
                    createRocketActionProperty(TEXT, TEXT, "Текст", true),
                    createRocketActionProperty(DESCRIPTION, DESCRIPTION, "Описание", false),
            )

    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
                settings.settings()[TEXT]?.takeIf { it.isNotEmpty() }?.let { text ->
                    val description = settings.settings()[DESCRIPTION]
                    object : RocketAction {
                        override fun contains(search: String): Boolean =
                                label.contains(search, ignoreCase = true)
                                        .or(text.contains(search, ignoreCase = true))
                                        .or(description?.contains(search, ignoreCase = true) ?: false)

                        override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                !(settings.id() == actionSettings.id() &&
                                        settings.settings() == actionSettings.settings())

                        override fun component(): Component = JMenu(label).apply {
                            this.icon = IconRepositoryFactory.repository.by(AppIcon.TEXT)
                            this.add(
                                    JTextPane().apply {
                                        this.text = text
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
                            )
                        }
                    }
                }
            }

    override fun type(): RocketActionType = RocketActionType { "SHOW_TEXT_AS_MENU" }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val TEXT = "text"
    }

    override fun name(): String = "Отобразить текст как подпункт меню"
}