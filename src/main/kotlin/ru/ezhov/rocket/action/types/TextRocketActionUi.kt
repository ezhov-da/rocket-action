package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel

class TextRocketActionUi : AbstractRocketAction() {
    override fun description(): String = "Show text"

    override fun properties(): List<RocketActionConfigurationProperty> =
            listOf(createRocketActionProperty(LABEL, "Text to display", true))

    override fun create(settings: RocketActionSettings): Component {
        val text = ConfigurationUtil.getValue(settings.settings(), LABEL)
        val label = JLabel(text)
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON3) {
                    val defaultToolkit = Toolkit.getDefaultToolkit()
                    val clipboard = defaultToolkit.systemClipboard
                    clipboard.setContents(StringSelection(text), null)
                    NotificationFactory.notification.show(NotificationType.INFO, "Text copy to clipboard")
                }
            }
        })
        return label
    }

    override fun type(): String = "SHOW_TEXT"

    companion object {
        private const val LABEL = "label"
    }
}