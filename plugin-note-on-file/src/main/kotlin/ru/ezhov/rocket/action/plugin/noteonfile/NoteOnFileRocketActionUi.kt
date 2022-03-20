package ru.ezhov.rocket.action.plugin.noteonfile

import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import java.awt.Component
import java.io.File
import java.util.UUID
import javax.swing.Icon
import javax.swing.JMenu

private val logger = KotlinLogging.logger {}

class NoteOnFileRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private val iconDef = IconRepositoryFactory.repository.by(AppIcon.TEXT)

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[PATH_AND_NAME]
            ?.takeIf {
                if (it.isEmpty()) {
                    logger.info { "Path and name note on file is empty" }
                }
                it.isNotEmpty()
            }
            ?.let { path ->
                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }
                    ?: path.let { File(path).name }
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: path
                val loadTextOnInitialize = settings.settings()[LOAD_TEXT_ON_INITIALIZE]?.toBoolean() ?: true

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                        path.contains(search, ignoreCase = true)
                            .or(label.contains(search, ignoreCase = true))
                            .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component =
                        JMenu(label).apply {
                            this.icon = iconDef
                            this.add(
                                TextPanel(
                                    path = path,
                                    label = label,
                                    loadOnInitialize = loadTextOnInitialize,
                                    style = settings.settings()[SYNTAX_STYLE],
                                    addStyleSelected = false,
                                )
                            )
                        }
                }
            }

    override fun type(): RocketActionType = RocketActionType { "NOTE_ON_FILE" }

    override fun description(): String = "Записка в файле. " +
        "Позволяет сохранять информацию в файл, а так же иметь быстрый доступ к файлу"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(
        LABEL,
        PATH_AND_NAME,
        DESCRIPTION,
    )

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = "Заголовок в меню",
                description = "Заголовок в меню",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(),
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = "Описание",
                description = """Описание, которое будет всплывать при наведении, 
                            |в случае отсутствия будет отображаться путь""".trimMargin(),
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec(),
            ),
            createRocketActionProperty(
                key = PATH_AND_NAME,
                name = "Путь к файлу и его название",
                description = "Путь по которому будет располагаться файл",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(
                    defaultValue = File("./notes/${UUID.randomUUID()}.txt").path
                ),
            ),
            createRocketActionProperty(
                key = SYNTAX_STYLE,
                name = "Стиль подсветки",
                description = "Подсветка синтаксиса по умолчанию",
                required = false,
                property = RocketActionPropertySpec.ListPropertySpec(
                    defaultValue = SyntaxConstants.SYNTAX_STYLE_NONE,
                    valuesForSelect = StylesList.styles,
                ),
            ),
            createRocketActionProperty(
                key = LOAD_TEXT_ON_INITIALIZE,
                name = "Загружать текст из файла при инициализации",
                description = """При установке true загружает текст при инициализации. 
                    "В случае, если текста много, может повлиять на первоначальную загрузку""",
                required = true,
                property = RocketActionPropertySpec.BooleanPropertySpec(
                    defaultValue = true,
                ),
            ),
        )
    }

    override fun name(): String = "Записка в файле"

    override fun icon(): Icon? = iconDef

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val SYNTAX_STYLE = RocketActionConfigurationPropertyKey("syntaxStyle")
        private val LOAD_TEXT_ON_INITIALIZE = RocketActionConfigurationPropertyKey("loadTextOnInitialize")
        private val PATH_AND_NAME = RocketActionConfigurationPropertyKey("pathAndName")
    }
}