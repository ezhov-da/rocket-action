package ru.ezhov.rocket.action.application.chainaction.startup.domain

import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunResult
import java.io.File

interface StartupActionLogRepository {
    /**
     * File with the accumulated run log, or null when no run has been written yet.
     */
    fun get(): File?

    /**
     * Appends a finished run (trigger source and per-entry outcomes) to the log.
     */
    fun save(result: StartupRunResult)
}
