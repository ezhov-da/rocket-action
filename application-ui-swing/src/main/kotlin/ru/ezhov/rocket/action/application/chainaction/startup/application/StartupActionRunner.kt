package ru.ezhov.rocket.action.application.chainaction.startup.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionLogRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRunStarter
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionEntryFinishedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionEntryStartedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionRunFinishedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionRunStartedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntryResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunState
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunStatus
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

/**
 * Executes the saved startup list.
 *
 * A run works on a snapshot of the saved list, executes enabled entries sequentially and
 * publishes lifecycle events. The finish event is always published, so the Startup window
 * can never stay locked after an unexpected error.
 */
@Service
class StartupActionRunner(
    private val startupActionsRepository: StartupActionsRepository,
    private val startupActionService: StartupActionService,
    private val startupActionLogRepository: StartupActionLogRepository,
    private val actionExecutorService: ActionExecutorService,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
) : StartupActionsRunStarter {
    private val running = AtomicBoolean(false)

    @Volatile
    private var current = 0

    @Volatile
    private var total = 0

    @Volatile
    private var lastResult: StartupRunResult? = null

    fun state(): StartupRunState = StartupRunState(
        running = running.get(),
        current = current,
        total = total,
        lastResult = lastResult,
    )

    fun isRunning(): Boolean = running.get()

    override fun startAsync(trigger: StartupRunTrigger) {
        if (!begin(trigger)) {
            return
        }

        Thread({ execute(trigger) }, "startup-actions-runner")
            .apply { isDaemon = true }
            .start()
    }

    /**
     * Runs the saved list in the calling thread. Intended for tests and internal use.
     *
     * @return the run result, or null when the run was not started (already running or disabled).
     */
    fun runBlocking(trigger: StartupRunTrigger): StartupRunResult? {
        if (!begin(trigger)) {
            return null
        }

        return execute(trigger)
    }

    private fun begin(trigger: StartupRunTrigger): Boolean {
        if (trigger == StartupRunTrigger.STARTUP && !automaticExecutionEnabled()) {
            logger.info {
                "Automatic startup actions are disabled by '${UsedPropertiesName.STARTUP_ACTIONS_ENABLED.propertyName}'"
            }
            return false
        }

        if (!running.compareAndSet(false, true)) {
            logger.info { "Startup actions are already running, the new run is ignored" }
            return false
        }

        return true
    }

    private fun automaticExecutionEnabled(): Boolean =
        generalPropertiesRepository.asBoolean(UsedPropertiesName.STARTUP_ACTIONS_ENABLED, true)

    private fun execute(trigger: StartupRunTrigger): StartupRunResult {
        val startedAt = LocalDateTime.now()
        val snapshot = startupActionsRepository.all()
        val enabledEntries = snapshot.startupActions.filter { it.enabled }
        val results = mutableListOf<StartupActionEntryResult>()

        snapshot.startupActions.forEach { entry ->
            results += StartupActionEntryResult(
                entry = entry,
                status = if (entry.enabled) StartupRunStatus.NOT_RUN else StartupRunStatus.SKIPPED,
            )
        }

        current = 0
        total = enabledEntries.size
        DomainEventFactory.publisher.publish(
            listOf(StartupActionRunStartedDomainEvent(trigger = trigger, total = enabledEntries.size))
        )

        var finishedResult: StartupRunResult? = null
        try {
            var stop = false
            enabledEntries.forEachIndexed { index, entry ->
                current = index + 1

                if (stop) {
                    updateResult(results, entry.id, StartupRunStatus.SKIPPED)
                    publishEntryFinished(entry.id, StartupRunStatus.SKIPPED, null)
                    return@forEachIndexed
                }

                DomainEventFactory.publisher.publish(
                    listOf(
                        StartupActionEntryStartedDomainEvent(
                            entryId = entry.id,
                            index = index + 1,
                            total = enabledEntries.size,
                        )
                    )
                )

                val action = startupActionService.actionById(entry.actionId)
                if (action == null) {
                    val message = "Action '${entry.actionId}' not found. The entry is skipped"
                    logger.warn { message }
                    updateResult(results, entry.id, StartupRunStatus.UNRESOLVED, error = message)
                    publishEntryFinished(entry.id, StartupRunStatus.UNRESOLVED, message)
                    return@forEachIndexed
                }

                updateResult(results, entry.id, StartupRunStatus.RUNNING)
                val input = if (startupActionService.acceptsInput(action)) entry.input else null
                val outcome = executeAction(action = action, input = input)
                updateResult(
                    results = results,
                    entryId = entry.id,
                    status = outcome.status,
                    result = outcome.result,
                    error = outcome.error,
                )
                publishEntryFinished(entry.id, outcome.status, outcome.error)

                if (outcome.status == StartupRunStatus.ERROR) {
                    logger.warn { "Startup action '${entry.actionId}' failed: ${outcome.error}" }
                    if (snapshot.stopOnError) {
                        stop = true
                    }
                }
            }

            finishedResult = StartupRunResult(
                trigger = trigger,
                startedAt = startedAt,
                finishedAt = LocalDateTime.now(),
                entries = results.toList(),
            )
        } catch (ex: Throwable) {
            logger.error(ex) { "Unexpected error when executing startup actions" }
            enabledEntries.getOrNull(current - 1)?.let { entry ->
                updateResult(
                    results = results,
                    entryId = entry.id,
                    status = StartupRunStatus.ERROR,
                    error = ex.toText(),
                )
            }
            finishedResult = StartupRunResult(
                trigger = trigger,
                startedAt = startedAt,
                finishedAt = LocalDateTime.now(),
                entries = results.toList(),
            )
        } finally {
            val result = finishedResult ?: StartupRunResult(
                trigger = trigger,
                startedAt = startedAt,
                finishedAt = LocalDateTime.now(),
                entries = results.toList(),
            )

            lastResult = result
            try {
                startupActionLogRepository.save(result)
            } catch (ex: Exception) {
                logger.error(ex) { "Can not save the startup actions log" }
            }

            running.set(false)
            current = 0
            total = 0

            DomainEventFactory.publisher.publish(listOf(StartupActionRunFinishedDomainEvent(result)))
        }

        return finishedResult!!
    }

    private fun executeAction(action: Action, input: String?): EntryOutcome {
        var finished = false
        var executionResult: Any? = null
        var failure: Exception? = null

        try {
            actionExecutorService.actionExecutor.execute(
                input,
                action,
                object : ProgressExecutingAction {
                    override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                        if (finished) {
                            return
                        }
                        finished = true
                        executionResult = result
                    }

                    override fun onAtomicActionSuccess(
                        orderId: String,
                        result: Any?,
                        atomicAction: AtomicAction,
                    ) {
                        // Intermediate chain step: the entry outcome is reported by onComplete/onAtomicActionFailure.
                    }

                    override fun onAtomicActionFailure(
                        orderId: String,
                        atomicAction: AtomicAction?,
                        ex: Exception,
                    ) {
                        if (finished) {
                            return
                        }
                        finished = true
                        failure = ex
                    }
                }
            )
        } catch (ex: Throwable) {
            return EntryOutcome(
                status = StartupRunStatus.ERROR,
                result = null,
                error = ex.toText(),
            )
        }

        val error = failure
        return if (error == null) {
            EntryOutcome(
                status = StartupRunStatus.SUCCESS,
                result = executionResult?.toString(),
                error = null,
            )
        } else {
            EntryOutcome(
                status = StartupRunStatus.ERROR,
                result = null,
                error = error.toText(),
            )
        }
    }

    private fun updateResult(
        results: MutableList<StartupActionEntryResult>,
        entryId: String,
        status: StartupRunStatus,
        result: String? = null,
        error: String? = null,
    ) {
        val index = results.indexOfFirst { it.entry.id == entryId }
        if (index < 0) {
            return
        }

        val previous = results[index]
        results[index] = previous.copy(
            status = status,
            result = result ?: previous.result,
            error = error ?: previous.error,
        )
    }

    private fun publishEntryFinished(entryId: String, status: StartupRunStatus, error: String?) {
        DomainEventFactory.publisher.publish(
            listOf(
                StartupActionEntryFinishedDomainEvent(
                    entryId = entryId,
                    status = status,
                    error = error,
                )
            )
        )
    }

    private data class EntryOutcome(
        val status: StartupRunStatus,
        val result: String?,
        val error: String?,
    )
}

private fun Throwable.toText(): String =
    this.message?.takeIf { it.isNotBlank() } ?: this.stackTraceToString()
