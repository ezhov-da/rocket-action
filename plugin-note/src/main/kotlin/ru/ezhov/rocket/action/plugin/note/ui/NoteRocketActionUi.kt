package ru.ezhov.rocket.action.plugin.note.ui

import mu.KotlinLogging
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
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.note.application.NoteApplicationService
import ru.ezhov.rocket.action.plugin.note.infrastructure.NoteRepositoryFactory
import java.awt.Component
import java.io.File
import java.nio.file.Path
import java.util.*
import javax.swing.Icon
import javax.swing.JMenu
import javax.swing.SwingWorker
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

private val logger = KotlinLogging.logger {}

class NoteRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-note.properties"))
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

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? = run {
        settings.settings()[LABEL]
            ?.takeIf { it.isNotEmpty() }
            ?.let { label ->
                settings.settings()[PATH]
                    ?.let { pathConfig ->
                        val path = pathConfig.ifEmpty { File("").absolutePath }
                        val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: label
                        // TODO correct to JMenuItem
                        val menu = JMenu(label)
                        settings.settings()[ICON_URL].let { icon ->
                            menu.icon = actionContext!!.icon().load(
                                iconUrl = icon ?: "",
                                defaultIcon = actionContext!!.icon().by(AppIcon.CLIPBOARD)
                            )
                        }
                        menu.toolTipText = description

                        runDialog(Path(path))

                        object : RocketAction {
                            override fun contains(search: String): Boolean = false

                            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                !(settings.id() == actionSettings.id() &&
                                    settings.settings() == actionSettings.settings())

                            override fun component(): Component = menu
                        }
                    }
            }
    }

    private fun checkAndCopyDb(path: String): File {
        val fileName = "notes.s3db"
        val fileDb = File(path, fileName)
        if (!fileDb.exists()) {
            this.javaClass.getResourceAsStream("/$fileName")?.use { ins ->
                fileDb.writeBytes(ins.readBytes())

                logger.debug { "Notes DB coped to '${fileDb.absolutePath}'" }
            }
        } else {
            logger.debug { "Notes DB '${fileDb.absolutePath}' already exists" }
        }

        return fileDb
    }

    private fun runDialog(pathToDb: Path) {
        object : SwingWorker<NoteApplicationService, Any>() {
            override fun doInBackground(): NoteApplicationService = run {
                val dbFile = checkAndCopyDb(pathToDb.absolutePathString())

                NoteApplicationService(
                    NoteRepositoryFactory.sqLiteNoteRepository(dbFile)
                )
            }

            override fun done() {
                try {
                    NoteDialog(
                        noteApplicationService = this.get(),
                        actionContext = actionContext!!,
                    ).isVisible = true
                } catch (ex: Exception) {
                    logger.warn(ex) { "Error when load notes by DB path '${pathToDb.absolutePathString()}'" }
                    actionContext!!
                        .notification()
                        .show(
                            NotificationType.WARN,
                            "Error creating \"Notes\""
                        )
                }
            }
        }.execute()
    }

    override fun type(): RocketActionType = RocketActionType { TYPE }

    override fun description(): String = "Storage of small notes, links, pieces of code. With tags and search"

    override fun asString(): List<String> = listOf(LABEL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = LABEL, name = LABEL, description = "Title", required = true, property = RocketActionPropertySpec.StringPropertySpec(defaultValue = "Notes")),
            createRocketActionProperty(key = DESCRIPTION, name = DESCRIPTION, description = "Description", required = false),
            createRocketActionProperty(key = ICON_URL, name = ICON_URL, description = "Icon URL", required = false),
            createRocketActionProperty(
                key = PATH,
                name = PATH,
                description = "Saved data storage path",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(defaultValue = File("").absolutePath),
            )
        )
    }

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.CLIPBOARD)

    override fun name(): String = "Notes"

    companion object {
        const val TYPE = "NOTE"
        private val LABEL = "label"
        private val ICON_URL = "iconUrl"
        private val DESCRIPTION = "description"
        private val PATH = "path"
    }
}
