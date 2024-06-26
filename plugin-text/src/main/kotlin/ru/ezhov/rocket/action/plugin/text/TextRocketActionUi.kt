package ru.ezhov.rocket.action.plugin.text

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JTextPane

class TextRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-text.properties"))
        object : RocketActionPluginInfo {
            override fun version(): String = properties.getProperty("version")

            override fun author(): String = properties.getProperty("author")

            override fun link(): String? = properties.getProperty("link")
        }
    }

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun description(): String = "Show text"

    override fun properties(): List<RocketActionConfigurationProperty> =
        listOf(
            createRocketActionProperty(
                key = LABEL,
                name = LABEL,
                description = "Text to display",
                required = true
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = DESCRIPTION,
                description = "Description",
                required = false
            )
        )

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
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
                            actionContext!!.notification().show(NotificationType.INFO, "Text copied to clipboard")
                        }
                    }
                })
            }

            context.search().register(settings.id(), label)
            description?.let { context.search().register(settings.id(), it) }

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

    override fun asString(): List<String> = listOf(LABEL)

    override fun name(): String = "Display text"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.TEXT)

    companion object {
        private val LABEL = "label"
        private val DESCRIPTION = "description"
    }

}
