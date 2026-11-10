package ru.ezhov.rocket.action.application.chainaction.startup.domain.model

import java.time.LocalDateTime

/**
 * Ordered list of actions executed when the application starts.
 *
 * The order of [startupActions] is the execution order.
 */
data class StartupActions(
    val stopOnError: Boolean = false,
    val startupActions: List<StartupActionEntry> = emptyList(),
)

/**
 * A single entry of the startup list.
 *
 * [id] identifies the entry (the same action can be added more than once),
 * [actionId] references an AtomicAction or a ChainAction.
 */
data class StartupActionEntry(
    val id: String,
    val actionId: String,
    val input: String? = null,
    val enabled: Boolean = true,
)

enum class StartupRunTrigger {
    STARTUP,
    MANUAL,
}

enum class StartupRunStatus {
    /** No run has touched the entry yet. */
    NOT_RUN,

    /** The entry is being executed right now. */
    RUNNING,

    SUCCESS,

    /** The execution of the action failed. Only this status is affected by `stopOnError`. */
    ERROR,

    /** The entry is disabled or was not executed because a previous entry stopped the run. */
    SKIPPED,

    /** The referenced action does not exist. This is a configuration warning, not an execution failure. */
    UNRESOLVED,
}

data class StartupActionEntryResult(
    val entry: StartupActionEntry,
    val status: StartupRunStatus,
    val result: String? = null,
    val error: String? = null,
)

data class StartupRunResult(
    val trigger: StartupRunTrigger,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime? = null,
    val entries: List<StartupActionEntryResult> = emptyList(),
) {
    val successCount: Int
        get() = entries.count { it.status == StartupRunStatus.SUCCESS }

    val errorCount: Int
        get() = entries.count { it.status == StartupRunStatus.ERROR }

    val skippedCount: Int
        get() = entries.count { it.status == StartupRunStatus.SKIPPED }

    val unresolvedCount: Int
        get() = entries.count { it.status == StartupRunStatus.UNRESOLVED }

    val isFinished: Boolean
        get() = finishedAt != null
}

/**
 * Current state of the startup runner: whether a run is active and the result of the last run.
 */
data class StartupRunState(
    val running: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val lastResult: StartupRunResult? = null,
) {
    companion object {
        fun idle(lastResult: StartupRunResult? = null): StartupRunState =
            StartupRunState(running = false, current = 0, total = 0, lastResult = lastResult)
    }
}
