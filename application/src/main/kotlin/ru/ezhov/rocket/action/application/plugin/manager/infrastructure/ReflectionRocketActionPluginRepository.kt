package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import java.awt.Component
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class ReflectionRocketActionPluginRepository : RocketActionPluginRepository {
    private var list: MutableList<RocketActionPlugin> = mutableListOf()
    private val configs = listOf(
        "ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi",
        "ru.ezhov.rocket.action.plugin.copytoclipboard.CopyToClipboardRocketActionUi",
        "ru.ezhov.rocket.action.plugin.exec.ExecRocketActionUi",
        "ru.ezhov.rocket.action.plugin.gist.GistRocketActionUi",
        "ru.ezhov.rocket.action.plugin.note.ui.NoteRocketActionUi",
        "ru.ezhov.rocket.action.plugin.openfile.OpenFileRocketActionUi",
        "ru.ezhov.rocket.action.plugin.openurl.OpenUrlRocketActionUi",
        "ru.ezhov.rocket.action.plugin.openurl.OpenUrlWithTextHistoryRocketActionUi",
        "ru.ezhov.rocket.action.plugin.openurl.OpenUrlWithTextRocketActionUi",
        "ru.ezhov.rocket.action.plugin.script.kotlin.ui.KotlinScriptRocketActionUi",
        "ru.ezhov.rocket.action.plugin.separator.SeparatorRocketActionUi",
        "ru.ezhov.rocket.action.plugin.showimage.ShowImageRocketActionUi",
        "ru.ezhov.rocket.action.plugin.showimagesvg.ShowSvgImageRocketActionUi",
        "ru.ezhov.rocket.action.plugin.template.CopyToClipboardTemplateRocketActionUi",
        "ru.ezhov.rocket.action.plugin.text.TextAsMenuRocketActionUi",
        "ru.ezhov.rocket.action.plugin.text.TextRocketActionUi",
        "ru.ezhov.rocket.action.plugin.todoist.TodoistRocketActionUi",
        "ru.ezhov.rocket.action.plugin.urlparser.UrlParserRocketActionUi",
        "ru.ezhov.rocket.action.plugin.noteonfile.NoteOnFileRocketActionUi",
    )

    private fun load() = runBlocking {
        val times = measureTimeMillis {
            logger.info { "Initialise configuration rocket action repository" }
            list = configs
                .map { classAsName ->
                    async { loadPlugin(classAsName) }
                }
                .awaitAll()
                .filterNotNull()
                .toMutableList()
        }
        logger.info { "Configuration rocket action repository initialize successful. timeMs=$times count=${list.size}" }
    }

    private fun loadPlugin(classAsName: String): RocketActionPlugin? {
        var rap: RocketActionPlugin? = null
        val initTimeClass = measureTimeMillis {
            try {
                logger.debug { "Initialize class='$classAsName'} run..." }

                val clazz = Class.forName(classAsName)
                val plugin = clazz.newInstance() as RocketActionPlugin
                rap = RocketActionPluginDecorator(plugin)
            } catch (e: InstantiationException) {
                logger.warn(e) { "Error when load class $classAsName" }
            } catch (e: IllegalAccessException) {
                logger.warn(e) { "Error when load class $classAsName" }
            } catch (e: NoSuchMethodException) {
                logger.warn(e) { "Error when load class $classAsName" }
            } catch (e: Exception) {
                logger.warn(e) { "Error when load class $classAsName" }
            }
        }

        logger.debug { "Initialize timeMs='$initTimeClass' for class='$classAsName'}" }

        return rap
    }

    override fun all(): List<RocketActionPlugin> {
        if (list.isEmpty()) {
            load();
        }
        return list
    }

    override fun by(type: RocketActionType): RocketActionPlugin? =
        all().firstOrNull { r: RocketActionPlugin -> r.configuration().type().value() == type.value() }
}

private class RocketActionPluginDecorator(
    private val rocketActionPluginOriginal: RocketActionPlugin
) : RocketActionPlugin {
    override fun factory(): RocketActionFactoryUi = RocketActionFactoryUiDecorator(
        rocketActionFactoryUi = rocketActionPluginOriginal.factory()
    )

    override fun configuration(): RocketActionConfiguration = rocketActionPluginOriginal.configuration()
}

private class RocketActionFactoryUiDecorator(
    private val rocketActionFactoryUi: RocketActionFactoryUi
) : RocketActionFactoryUi {
    override fun create(settings: RocketActionSettings): RocketAction? =
        rocketActionFactoryUi.create(settings = settings)
            ?.let { ra ->
                RocketActionDecorator(originalRocketAction = ra)
            }

    override fun type(): RocketActionType = rocketActionFactoryUi.type()
}

private class RocketActionDecorator(
    private val originalRocketAction: RocketAction
) : RocketAction {
    companion object {
        const val MAX_TIME__GET_COMPONENT_IN_MILLS = 2
    }

    override fun contains(search: String): Boolean = originalRocketAction.contains(search = search)

    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
        originalRocketAction.isChanged(actionSettings = actionSettings)

    override fun component(): Component {
        val component: Component
        val timeInMillis = measureTimeMillis {
            component = originalRocketAction.component()
        }

        if (timeInMillis > MAX_TIME__GET_COMPONENT_IN_MILLS) {
            logger.warn {
                "Getting component for action was over '$MAX_TIME__GET_COMPONENT_IN_MILLS' milliseconds. " +
                    "This can slow down the application"
            }
        }

        return component
    }
}
