package ru.ezhov.rocket.action.application.chainaction.scheduler.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.ActionSchedulerRepository
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulers
import ru.ezhov.rocket.action.application.chainaction.scheduler.infrastructure.model.JsonActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.infrastructure.model.JsonActionSchedulers
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File

private val logger = KotlinLogging.logger {}

@Component
class JsonFileActionSchedulerRepository(
    generalPropertiesRepository: GeneralPropertiesRepository,
    private val objectMapper: ObjectMapper,
    folderPathOverride: String? = null,
) : ActionSchedulerRepository {
    val lock = Any()

    private val folderPath =
        folderPathOverride
            ?: generalPropertiesRepository
                .asStringOrNull(UsedPropertiesName.ACTION_SCHEDULERS_FILE_FOLDER_REPOSITORY_PATH)
            ?: "./.rocket-action/action-schedulers"

    private var allCached: ActionSchedulers? = null
    private var indexCached: Map<String, ActionScheduler>? = null

    override fun all(): ActionSchedulers {
        synchronized(lock) {
            allCached?.let { return it }

            val schedulers = readSchedulers()
            cache(schedulers)
            return schedulers
        }
    }

    override fun byActionId(actionId: String): ActionScheduler? {
        synchronized(lock) {
            if (indexCached == null) {
                cache(readSchedulers())
            }
            return indexCached?.get(actionId)
        }
    }

    private fun readSchedulers(): ActionSchedulers {
        val files = folder().listFiles { _, name -> name.endsWith(".json") }.orEmpty()
        val schedulers =
            files
                .mapNotNull { f ->
                    try {
                        objectMapper.readValue(f, JsonActionScheduler::class.java).toActionScheduler()
                    } catch (ex: Exception) {
                        logger.warn(ex) { "Error when read action scheduler from file '${f.absolutePath}'. Return null" }
                        null
                    }
                }

        return ActionSchedulers(schedulers)
    }

    private fun cache(schedulers: ActionSchedulers) {
        allCached = schedulers
        indexCached = schedulers.schedulers.associateBy { it.actionId }
    }

    private fun invalidate() {
        allCached = null
        indexCached = null
    }

    /**
     * The read path must not create the folder as a side effect.
     */
    private fun folder(): File = File(folderPath)

    private fun writableFolder(): File {
        val file = File(folderPath)
        if (!file.exists()) {
            file.mkdirs()
        }

        return file
    }

    override fun save(schedulers: ActionSchedulers) {
        synchronized(lock) {
            schedulers.schedulers.forEach { scheduler ->
                writeScheduler(scheduler)
            }
            invalidate()
        }
    }

    override fun save(scheduler: ActionScheduler) {
        synchronized(lock) {
            writeScheduler(scheduler)
            invalidate()
        }
    }

    override fun delete(actionId: String) {
        synchronized(lock) {
            val file = File(folder(), "$actionId.json")
            if (file.exists()) {
                file.delete()
            }
            invalidate()
        }
    }

    private fun writeScheduler(scheduler: ActionScheduler) {
        val file = actionSchedulerFile(scheduler.actionId)
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, scheduler)
    }

    private fun actionSchedulerFile(actionId: String) =
        File(writableFolder(), "$actionId.json")
}

private fun JsonActionSchedulers.toActionSchedulers() =
    ActionSchedulers(
        schedulers = this.schedulers.map {
            ActionScheduler(
                actionId = it.actionId,
                cron = it.cron,
                dateOfTheLastLaunch = it.dateOfTheLastLaunch,
                schedulerStatus = it.schedulerStatus,
            )
        }
    )

private fun JsonActionScheduler.toActionScheduler() =
    ActionScheduler(
        actionId = this.actionId,
        cron = this.cron,
        dateOfTheLastLaunch = this.dateOfTheLastLaunch,
        schedulerStatus = this.schedulerStatus,
    )
