package ru.ezhov.rocket.action.plugin.template

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.template.infrastructure.VelocityEngineImpl
import java.awt.Component
import javax.swing.Icon
import javax.swing.JMenu

class CopyToClipboardTemplateRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
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
        settings.settings()[TEXT]?.takeIf { it.isNotEmpty() }?.let { text ->
            val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: text
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: text

            val notePanelEngine = NotePanelEngine(
                originText = text,
                engine = VelocityEngineImpl(),
                context = context
            )
            val menu = JMenu(label)
            menu.icon = actionContext!!.icon().by(AppIcon.CLIPBOARD)
            menu.toolTipText = description
            menu.add(notePanelEngine)

            context.search().register(settings.id(), label)
            context.search().register(settings.id(), description)

            object : RocketAction {
                override fun contains(search: String): Boolean =
                    label.contains(search, ignoreCase = true)
                        .or(description.contains(search, ignoreCase = true))

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = menu
            }
        }

    override fun name(): String = "Copy to clipboard by template"

    override fun type(): RocketActionType = RocketActionType { "COPY_TO_CLIPBOARD_TEMPLATE" }

    override fun description(): String = "Allows you to copy text to the clipboard with the indication of variables"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = LABEL, name = LABEL, description = "Header", required = false),
            createRocketActionProperty(key = DESCRIPTION, name = DESCRIPTION, description = "Description", required = false),
            createRocketActionProperty(key = TEXT, name = TEXT, description = "Template for copy", required = true)
        )
    }

    override fun asString(): List<String> = listOf(LABEL, TEXT)

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.CLIPBOARD)

    companion object {
        private val LABEL = "label"
        private val DESCRIPTION = "description"
        private val TEXT = "text"
    }
}
