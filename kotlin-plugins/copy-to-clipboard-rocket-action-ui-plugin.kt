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
import javax.swing.Icon
import javax.swing.JMenuItem

class CopyToClipboardRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    companion object {
        private val LABEL = "label"
        private val DESCRIPTION = "description"
        private val TEXT = "text"
    }

    override fun info(): RocketActionPluginInfo = object : RocketActionPluginInfo {
            override fun version(): String = "version"

            override fun author(): String = "author"

            override fun link(): String? = "link"
        }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[TEXT]?.takeIf { it.isNotEmpty() }?.let { text ->
            actionContext = context

            val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }
                ?: text
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() }
                ?: text
            val menuItem = JMenuItem(label)
            menuItem.icon = actionContext!!.icon().by(AppIcon.CLIPBOARD)
            menuItem.toolTipText = description
            menuItem.addActionListener {
                val defaultToolkit = Toolkit.getDefaultToolkit()
                val clipboard = defaultToolkit.systemClipboard
                clipboard.setContents(StringSelection(text), null)
                context.notification().show(NotificationType.INFO, "Text copied to clipboard")
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

    override fun type(): RocketActionType = RocketActionType { "COPY_TO_CLIPBOARD_KOTLIN_PLUGIN" }

    override fun description(): String = "Allows you to copy text to the clipboard"

    override fun asString(): List<String> = listOf(LABEL, TEXT)

    override fun name(): String = "Copy to clipboard from kotlin script"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(LABEL, LABEL, "Text to display", false),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION, "Description", false),
            createRocketActionProperty(TEXT, TEXT, "Text to copy to clipboard", true)
        )
    }

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.CLIPBOARD)

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }
}

CopyToClipboardRocketActionUi()
