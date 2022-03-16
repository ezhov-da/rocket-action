package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
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

    private fun load() {
        val times = measureTimeMillis {
            logger.info { "Initialise configuration rocket action repository" }

            list = mutableListOf()

            configs.forEach { classAsName ->
                val initTimeClass = measureTimeMillis {
                    try {
                        val clazz = Class.forName(classAsName)
                        list.add(clazz.newInstance() as RocketActionPlugin)
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
            }
        }
        logger.info { "Configuration rocket action repository initialize successful. timeMs=$times count=${list.size}" }
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