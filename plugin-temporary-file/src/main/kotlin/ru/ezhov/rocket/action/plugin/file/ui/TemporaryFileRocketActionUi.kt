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
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.plugin.file.domain.TemporaryFileService
import java.awt.Component
import javax.swing.Icon
import javax.swing.JMenu

private val logger = KotlinLogging.logger {}

class TemporaryFileRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private val icon = IconRepositoryFactory.repository.by(AppIcon.FILE)

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[LABEL]
            ?.takeIf { it.isNotEmpty() }
            ?.let { label ->
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() }.orEmpty()

                val menu = JMenu(label)
                menu.icon = icon
                menu.toolTipText = description
                menu.add(TemporaryFileUi(temporaryFileService = TemporaryFileService()))

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

    override fun icon(): Icon? = icon

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
    }
}
