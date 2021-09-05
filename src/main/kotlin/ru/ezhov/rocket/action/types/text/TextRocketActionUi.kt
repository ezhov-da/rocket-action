package ru.ezhov.rocket.action.types.text

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JTextPane

class TextRocketActionUi : AbstractRocketAction() {
    override fun description(): String = "Show text"

    override fun properties(): List<RocketActionConfigurationProperty> =
            listOf(createRocketActionProperty(LABEL, LABEL, "Text to display", true))

    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
                object : RocketAction {
                    override fun contains(search: String): Boolean =
                            label.contains(search, ignoreCase = true)

                    override fun component(): Component = JTextPane().apply {
                        text = label
                        isEditable = false
                        background = JLabel().background
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseReleased(e: MouseEvent) {
                                if (e.button == MouseEvent.BUTTON3) {
                                    val defaultToolkit = Toolkit.getDefaultToolkit()
                                    val clipboard = defaultToolkit.systemClipboard
                                    clipboard.setContents(StringSelection(text), null)
                                    NotificationFactory.notification.show(NotificationType.INFO, "Text copy to clipboard")
                                }
                            }
                        })
                    }
                }
            }

    override fun type(): String = "SHOW_TEXT"

    companion object {
        private const val LABEL = "label"
    }

    override fun name(): String = "Отобразить текст"
}