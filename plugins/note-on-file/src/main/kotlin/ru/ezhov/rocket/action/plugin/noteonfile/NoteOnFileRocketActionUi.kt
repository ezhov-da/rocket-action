package ru.ezhov.rocket.action.plugin.noteonfile

import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import java.awt.Component
import java.io.File
import java.util.*
import javax.swing.Icon
import javax.swing.JMenu

private val logger = KotlinLogging.logger {}

class NoteOnFileRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-note-on-file.properties"))
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
        settings.settings()[PATH_AND_NAME]
            ?.takeIf {
                if (it.isEmpty()) {
                    logger.info { "Path and name note on file is empty" }
                }
                it.isNotEmpty()
            }
            ?.let { path ->
                actionContext = context

                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }
                    ?: path.let { File(path).name }
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: path
                val loadTextOnInitialize = settings.settings()[LOAD_TEXT_ON_INITIALIZE]?.toBoolean() ?: true
                val delimiter = settings.settings()[DELIMITER].orEmpty()

                val autoSave = settings.settings()[AUTO_SAVE]?.toBooleanStrictOrNull()
                val autoSaveInSeconds = settings.settings()[AUTO_SAVE_PERIOD_IN_SECOND]?.toIntOrNull()

                val component = JMenu(label).apply {
                    this.icon = actionContext!!.icon().by(AppIcon.TEXT)

                    val textPanelConfiguration = TextPanelConfiguration(
                        path = path,
                        label = label,
                        loadOnInitialize = loadTextOnInitialize,
                        style = settings.settings()[SYNTAX_STYLE],
                        addStyleSelected = false,
                        delimiter = delimiter,
                    )

                    this.add(
                        TextPanel(
                            textPanelConfiguration = textPanelConfiguration,
                            textAutoSave = autoSave?.let {
                                TextAutoSave(
                                    enable = it,
                                    delayInSeconds = autoSaveInSeconds ?: DEFAULT_AUTO_SAVE_PERIOD_IN_SECOND
                                )
                            },
                            context = context,
                        )
                    )
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

                    override fun component(): Component = component
                }
            }

    override fun type(): RocketActionType = RocketActionType { "NOTE_ON_FILE" }

    override fun description(): String = "Note in file. " +
        "Allows you to save information to a file, as well as have quick access to the file"

    override fun asString(): List<String> = listOf(
        LABEL,
        PATH_AND_NAME,
        DESCRIPTION,
    )

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = "Title in the menu",
                description = "Title in the menu",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(),
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = "Description",
                description = "A description that will pop up on hover, otherwise the path will be displayed",
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec(),
            ),
            createRocketActionProperty(
                key = PATH_AND_NAME,
                name = "File path and name",
                description = "The path where the file will be located",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(
                    defaultValue = File("./notes/${UUID.randomUUID()}.txt").path
                ),
            ),
            createRocketActionProperty(
                key = SYNTAX_STYLE,
                name = "Backlight Style",
                description = "Syntax highlighting by default",
                required = false,
                property = RocketActionPropertySpec.ListPropertySpec(
                    defaultValue = SyntaxConstants.SYNTAX_STYLE_NONE,
                    valuesForSelect = StylesList.styles,
                ),
            ),
            createRocketActionProperty(
                key = DELIMITER,
                name = "Group separator",
                description = "If a separator for groups is specified, the specified separator will be searched in " +
                    "the file and a list of groups will be built for quick transition",
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec(),
            ),
            createRocketActionProperty(
                key = LOAD_TEXT_ON_INITIALIZE,
                name = "Load text from file on initialization",
                description = "When set to true, loads the text on initialization. In case there is a lot of text, it may affect the initial load",
                required = true,
                property = RocketActionPropertySpec.BooleanPropertySpec(
                    defaultValue = true,
                ),
            ),
            createRocketActionProperty(
                key = AUTO_SAVE,
                name = "Save text automatically",
                description = "Automatic text saving",
                required = true,
                property = RocketActionPropertySpec.BooleanPropertySpec(
                    defaultValue = true,
                ),
            ),
            createRocketActionProperty(
                key = AUTO_SAVE_PERIOD_IN_SECOND,
                name = "Automatically save after a specified time in seconds",
                description = "Automatically save after a specified time in seconds",
                required = false,
                property = RocketActionPropertySpec.IntPropertySpec(
                    defaultValue = DEFAULT_AUTO_SAVE_PERIOD_IN_SECOND,
                ),
            ),
        )
    }

    override fun name(): String = "Note on file"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.TEXT)

    companion object {
        private val LABEL = "label"
        private val DESCRIPTION = "description"
        private val SYNTAX_STYLE = "syntaxStyle"
        private val LOAD_TEXT_ON_INITIALIZE = "loadTextOnInitialize"
        private val PATH_AND_NAME = "pathAndName"
        private val DELIMITER = "delimiter"
        private val AUTO_SAVE = "autoSave"
        private val AUTO_SAVE_PERIOD_IN_SECOND = "autoSavePeriodInSecond"
        private const val DEFAULT_AUTO_SAVE_PERIOD_IN_SECOND = 5
    }
}
