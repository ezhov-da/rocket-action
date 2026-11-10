package ru.ezhov.rocket.action.plugin.openfile

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
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import java.awt.Component
import java.awt.Desktop
import java.io.File
import java.util.*
import javax.swing.Icon
import javax.swing.JMenuItem

private val logger = KotlinLogging.logger {}

class OpenFileRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-open-file.properties"))
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
        settings.settings()[PATH]
            ?.takeIf { it.isNotEmpty() }
            ?.let { path ->
                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }
                    ?: path.let { File(path).name }
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: path

                val menuItem = JMenuItem(label)
                menuItem.icon = actionContext!!.icon().by(AppIcon.FILE)
                menuItem.toolTipText = description
                menuItem.addActionListener {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(File(path))
                        } catch (ex: Exception) {
                            logger.warn(ex) { "Error when open file '$path'" }
                            actionContext!!.notification().show(NotificationType.ERROR, "File opening error")
                        }
                    }
                }

                context.search().register(settings.id(), path)
                context.search().register(settings.id(), label)
                context.search().register(settings.id(), description)

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                        path.contains(search, ignoreCase = true)
                            .or(label.contains(search, ignoreCase = true))
                            .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = menuItem
                }
            }

    override fun type(): RocketActionType = RocketActionType { "OPEN_FILE" }

    override fun description(): String = "Open file"

    override fun asString(): List<String> = listOf(
        LABEL,
        PATH,
        DESCRIPTION,
    )

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = "Title",
                description = "The title to be displayed. If not present, the filename will be used.",
                required = false
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = "Description",
                    description = "A description that will pop up on hover, otherwise the path will be displayed",
                required = false
            ),
            createRocketActionProperty(
                key = PATH,
                name = "The path to the file",
                description = "The path where the file will be opened",
                required = true
            )
        )
    }

    override fun name(): String = "Open file"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.FILE)

    companion object {
        private val LABEL = "label"
        private val DESCRIPTION = "description"
        private val PATH = "path"
    }
}
