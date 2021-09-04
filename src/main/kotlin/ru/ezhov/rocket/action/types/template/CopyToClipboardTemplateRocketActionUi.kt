package ru.ezhov.rocket.action.types.template

import ru.ezhov.rocket.action.api.Action
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.SearchableAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.template.infrastructure.VelocityEngineImpl
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.types.ConfigurationUtil
import java.awt.Component
import javax.swing.JMenu

class CopyToClipboardTemplateRocketActionUi : AbstractRocketAction() {

    override fun create(settings: RocketActionSettings): Action {
        val label = ConfigurationUtil.getValue(settings.settings(), LABEL)
        val text = ConfigurationUtil.getValue(settings.settings(), TEXT)
        val notePanelEngine = NotePanelEngine(text, VelocityEngineImpl())
        val menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menu.icon = IconRepositoryFactory.repository.by(AppIcon.CLIPBOARD)
        menu.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        menu.add(notePanelEngine)
        return object : Action {
            override fun action(): SearchableAction = object : SearchableAction {
                override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
            }

            override fun component(): Component = menu
        }
    }

    override fun name(): String = "Копировать в буфер по шаблону"

    override fun type(): String = "COPY_TO_CLIPBOARD_TEMPLATE"

    override fun description(): String = "Allows you to copy a previously prepared text to the clipboard"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "Displayed title", true),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "Description that will be displayed as a hint", true),
                createRocketActionProperty(TEXT, TEXT, "Text prepared for copying to the clipboard", true)
        )
    }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val TEXT = "text"
    }
}