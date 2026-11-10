package ru.ezhov.rocket.action.application.chainaction.startup.infrastructure

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionLogRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunStatus
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * Appends every finished run to a single text log, so the history survives an application restart.
 */
@Component
class FileStartupActionLogRepository(
    private val file: File,
) : StartupActionLogRepository {
    private val lock = Any()

    @Autowired
    constructor(generalPropertiesRepository: GeneralPropertiesRepository) : this(
        file = File(
            generalPropertiesRepository.asStringOrNull(UsedPropertiesName.STARTUP_ACTIONS_LOG_FILE_PATH)
                ?: DEFAULT_FILE_PATH
        )
    )

    override fun get(): File? =
        synchronized(lock) {
            file.takeIf { it.exists() }
        }

    override fun save(result: StartupRunResult) {
        synchronized(lock) {
            file.parentFile?.mkdirs()
            file.appendText(buildRunLog(result))
        }
    }

    private fun buildRunLog(result: StartupRunResult): String = buildString {
        val startedAt = result.startedAt.format(DATE_TIME_FORMATTER)
        val finishedAt = result.finishedAt?.format(DATE_TIME_FORMATTER) ?: "-"
        appendLine("=== $startedAt ${result.trigger} started, finished at $finishedAt ===")
        appendLine(
            "success=${result.successCount}, error=${result.errorCount}, " +
                "skipped=${result.skippedCount}, unresolved=${result.unresolvedCount}"
        )
        result.entries.forEach { entryResult ->
            val details = when (entryResult.status) {
                StartupRunStatus.SUCCESS -> entryResult.result.orEmpty()
                StartupRunStatus.ERROR -> entryResult.error.orEmpty()
                StartupRunStatus.UNRESOLVED -> entryResult.error.orEmpty()
                else -> entryResult.error.orEmpty()
            }
            appendLine(
                "[${entryResult.status}] entry=${entryResult.entry.id} " +
                    "action=${entryResult.entry.actionId} $details"
            )
        }
        appendLine()
    }

    companion object {
        const val DEFAULT_FILE_PATH = "./.rocket-action/startup-actions/log.txt"
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
