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
) : ActionSchedulerRepository {
    val lock = Any()

    private val folderPath =
        generalPropertiesRepository
            .asStringOrNull(UsedPropertiesName.ACTION_SCHEDULERS_FILE_FOLDER_REPOSITORY_PATH)
            ?: "./.rocket-action/action-schedulers"

    override fun all(): ActionSchedulers {
        synchronized(lock) {
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
    }

    private fun folder(): File {
        val file = File(folderPath)
        if (!file.exists()) {
            file.mkdirs()
        }

        return file
    }

    private fun file(): File {
        val file = File(folderPath)
        if (!file.exists()) {
            file.parentFile.mkdirs()
        }

        return file
    }

    override fun save(schedulers: ActionSchedulers) {
        schedulers.schedulers.forEach { scheduler ->
            writeScheduler(scheduler)
        }
    }

    override fun save(scheduler: ActionScheduler) {
        writeScheduler(scheduler)
    }

    override fun delete(actionId: String) {
        actionSchedulerFile(actionId).let { f ->
            if (f.exists()) {
                f.delete()
            }
        }
    }

    private fun writeScheduler(scheduler: ActionScheduler) {
        synchronized(lock) {
            val file = actionSchedulerFile(scheduler.actionId)
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, scheduler)
        }
    }

    private fun actionSchedulerFile(actionId: String) =
        File(folder(), "$actionId.json")
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
