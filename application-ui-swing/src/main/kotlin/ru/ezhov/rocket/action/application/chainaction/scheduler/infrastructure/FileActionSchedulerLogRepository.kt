package ru.ezhov.rocket.action.application.chainaction.scheduler.infrastructure

import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.ActionSchedulerLogRepository
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Component
class FileActionSchedulerLogRepository(
    generalPropertiesRepository: GeneralPropertiesRepository,
) : ActionSchedulerLogRepository {
    val lock = Any()

    private val folderPath =
        generalPropertiesRepository
            .asStringOrNull(UsedPropertiesName.API_KEYS_FILE_REPOSITORY_PATH)
            ?: "./.rocket-action/action-schedulers/logs"


    private fun folder(): File {
        val file = File(folderPath)
        if (!file.exists()) {
            file.mkdirs()
        }

        return file
    }

    private fun actionSchedulerLogFile(actionId: String) =
        File(folder(), "$actionId.json")

    override fun get(actionId: String): File? {
        synchronized(lock) {
            return actionSchedulerLogFile(actionId).takeIf { it.exists() }
        }
    }

    override fun save(actionId: String, date: LocalDateTime, status: ActionSchedulerStatus, result: String?, ex: Exception?) {
        synchronized(lock) {
            val file = actionSchedulerLogFile(actionId)
            file.appendText(
                buildLogMsg(
                    date = date,
                    status = status,
                    result = result,
                    ex = ex,
                )
            )
        }
    }

    override fun delete(actionId: String) {
        actionSchedulerLogFile(actionId).takeIf { it.exists() }?.delete()
    }

    private fun buildLogMsg(
        date: LocalDateTime,
        status: ActionSchedulerStatus,
        result: String?,
        ex: Exception?
    ): String =
        "${date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))} $status ${
            result ?: ex?.stackTraceToString().orEmpty()
        }" + System.lineSeparator()
}
