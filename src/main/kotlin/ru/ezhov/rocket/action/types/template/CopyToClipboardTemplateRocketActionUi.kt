package ru.ezhov.rocket.action.types.template

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.template.infrastructure.VelocityEngineImpl
import ru.ezhov.rocket.action.types.AbstractRocketAction
import java.awt.Component
import javax.swing.JMenu

class CopyToClipboardTemplateRocketActionUi : AbstractRocketAction() {

    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[TEXT]?.takeIf { it.isNotEmpty() }?.let { text ->
                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: text
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: text

                val notePanelEngine = NotePanelEngine(text, VelocityEngineImpl())
                val menu = JMenu(label)
                menu.icon = IconRepositoryFactory.repository.by(AppIcon.CLIPBOARD)
                menu.toolTipText = description
                menu.add(notePanelEngine)

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

    override fun name(): String = "Копировать в буфер по шаблону"

    override fun type(): RocketActionType = RocketActionType { "COPY_TO_CLIPBOARD_TEMPLATE" }

    override fun description(): String = "Позволяет скопировать текст в буфер с указанием переменных"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "Заголовок", false),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "Описание", false),
                createRocketActionProperty(TEXT, TEXT, "Шаблон для копирования", true)
        )
    }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val TEXT = "text"
    }
}