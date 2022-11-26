package ru.ezhov.rocket.action.plugin.file.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.file.domain.TemporaryFileService
import java.awt.Component
import javax.swing.Icon
import javax.swing.JMenu

private val logger = KotlinLogging.logger {}

class TemporaryFileRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
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
        settings.settings()[LABEL]
            ?.takeIf { it.isNotEmpty() }
            ?.let { label ->
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() }.orEmpty()

                val menu = JMenu(label)
                menu.icon = context.icon().by(AppIcon.FILE)
                menu.toolTipText = description
                menu.add(TemporaryFileUi(temporaryFileService = TemporaryFileService(), context = context))

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
                            .or(label.contains(search, ignoreCase = true))
                            .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = menu
                }
            }

    override fun type(): RocketActionType = RocketActionType { "SAVE_TEMPORARY_FILE" }

    override fun description(): String = "Сохранить во временный файл"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(
        LABEL,
        DESCRIPTION,
    )

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = "Заголовок",
                description = "Заголовок, который будет отображаться",
                required = true
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = "Описание",
                description = """Описание, которое будет всплывать при наведении,
                            |в случае отсутствия будет отображаться путь""".trimMargin(),
                required = false
            ),
        )
    }

    override fun name(): String = "Сохранить во временный файл"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.FILE)

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
    }
}
