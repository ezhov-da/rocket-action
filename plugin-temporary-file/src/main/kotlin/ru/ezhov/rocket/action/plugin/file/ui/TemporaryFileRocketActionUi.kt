package ru.ezhov.rocket.action.plugin.file.ui

import mu.KotlinLogging
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
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.file.domain.TemporaryFileService
import java.awt.Component
import java.util.*
import javax.swing.Icon
import javax.swing.JMenu

private val logger = KotlinLogging.logger {}

class TemporaryFileRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-temporary-file.properties"))
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

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[LABEL]
            ?.takeIf { it.isNotEmpty() }
            ?.let { label ->
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() }.orEmpty()

                val menu = JMenu(label)
                menu.icon = context.icon().by(AppIcon.FILE)
                menu.toolTipText = description
                menu.add(TemporaryFileUi(temporaryFileService = TemporaryFileService(), context = context))

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

    override fun type(): RocketActionType = RocketActionType { "SAVE_TEMPORARY_FILE" }

    override fun description(): String = "Save to temporary file"

    override fun asString(): List<String> = listOf(
        LABEL,
        DESCRIPTION,
    )

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = "Title",
                description = "Title to be displayed",
                required = true
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = "Description",
                description = "A description that will pop up on hover, otherwise the path will be displayed",
                required = false
            ),
        )
    }

    override fun name(): String = "Save to temporary file"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.FILE)

    companion object {
        private val LABEL = "label"
        private val DESCRIPTION = "description"
    }
}
