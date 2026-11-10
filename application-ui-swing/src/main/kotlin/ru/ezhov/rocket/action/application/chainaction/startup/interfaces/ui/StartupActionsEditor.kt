package ru.ezhov.rocket.action.application.chainaction.startup.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.startup.application.StartupActionService
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunState
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunStatus

/**
 * Editable state of the Startup window.
 *
 * The editor keeps a draft of the saved list and knows whether the draft differs from the file.
 * It is a plain class without Swing dependencies, so its rules can be tested without a display.
 */
class StartupActionsEditor(
    private val startupActionService: StartupActionService,
    private val runStateProvider: () -> StartupRunState,
) {
    private var saved: StartupActions = StartupActions()

    var stopOnError: Boolean = false
        private set

    var entries: List<StartupActionEntry> = emptyList()
        private set

    var selection: Int = -1
        private set

    var runState: StartupRunState = runStateProvider()
        private set

    val dirty: Boolean
        get() = stopOnError != saved.stopOnError || entries != saved.startupActions

    val editable: Boolean
        get() = !runState.running

    /**
     * "Run now" works with the saved list only, so an unsaved draft does not count.
     */
    val canRunNow: Boolean
        get() = !runState.running && saved.startupActions.isNotEmpty()

    fun reload() {
        saved = startupActionService.startupActions()
        stopOnError = saved.stopOnError
        entries = saved.startupActions
        if (selection >= entries.size) {
            selection = entries.lastIndex
        }
    }

    fun refreshRunState() {
        runState = runStateProvider()
    }

    fun applyRunState(state: StartupRunState) {
        runState = state
    }

    fun select(index: Int) {
        selection = index.coerceIn(-1, entries.lastIndex)
    }

    fun selectedEntry(): StartupActionEntry? = entries.getOrNull(selection)

    fun addAction(actionId: String) {
        if (!editable) {
            return
        }

        entries = startupActionService.add(aggregate(), actionId).startupActions
        selection = entries.lastIndex
    }

    fun removeSelected() {
        if (!editable) {
            return
        }

        val entry = selectedEntry() ?: return
        entries = startupActionService.remove(aggregate(), entry.id).startupActions
        if (selection >= entries.size) {
            selection = entries.lastIndex
        }
    }

    fun moveUp() {
        if (!editable) {
            return
        }

        val entry = selectedEntry() ?: return
        entries = startupActionService.moveUp(aggregate(), entry.id).startupActions
        if (entries.indexOfFirst { it.id == entry.id } >= 0) {
            selection = entries.indexOfFirst { it.id == entry.id }
        }
    }

    fun moveDown() {
        if (!editable) {
            return
        }

        val entry = selectedEntry() ?: return
        entries = startupActionService.moveDown(aggregate(), entry.id).startupActions
        if (entries.indexOfFirst { it.id == entry.id } >= 0) {
            selection = entries.indexOfFirst { it.id == entry.id }
        }
    }

    fun setEnabled(enabled: Boolean) {
        if (!editable) {
            return
        }

        val entry = selectedEntry() ?: return
        entries = startupActionService.setEnabled(aggregate(), entry.id, enabled).startupActions
    }

    fun setInput(input: String?) {
        if (!editable) {
            return
        }

        val entry = selectedEntry() ?: return
        entries = startupActionService.setInput(aggregate(), entry.id, input).startupActions
    }

    fun setStopOnError(stopOnError: Boolean) {
        if (!editable) {
            return
        }

        this.stopOnError = startupActionService.setStopOnError(aggregate(), stopOnError).stopOnError
    }

    fun save() {
        val aggregate = aggregate()
        startupActionService.save(aggregate)
        saved = aggregate
    }

    fun acceptsInput(entry: StartupActionEntry): Boolean =
        startupActionService.acceptsInput(entry.actionId)

    fun actionName(actionId: String): String =
        startupActionService.actionName(actionId) ?: "Action not found"

    fun actionType(actionId: String): String =
        when (startupActionService.actionById(actionId)) {
            is ChainAction -> "Chain"
            is AtomicAction -> "Atomic"
            else -> "?"
        }

    fun isResolved(entry: StartupActionEntry): Boolean =
        startupActionService.isResolved(entry)

    fun availableActions() = startupActionService.actions()

    fun statusOf(entryId: String): StartupRunStatus {
        val result = runState.lastResult ?: return StartupRunStatus.NOT_RUN
        return result.entries.firstOrNull { it.entry.id == entryId }?.status ?: StartupRunStatus.NOT_RUN
    }

    fun errorOf(entryId: String): String? {
        val result = runState.lastResult ?: return null
        return result.entries.firstOrNull { it.entry.id == entryId }?.error
    }

    fun progressText(): String? {
        val state = runState
        return when {
            state.running -> "Startup actions are running (${state.current}/${state.total})..."
            else -> state.lastResult?.let { result ->
                val finished = if (result.isFinished) "finished" else "incomplete"
                "${result.trigger} run $finished: success=${result.successCount}, " +
                    "error=${result.errorCount}, skipped=${result.skippedCount}, " +
                    "unresolved=${result.unresolvedCount}"
            }
        }
    }

    private fun aggregate() = StartupActions(
        stopOnError = stopOnError,
        startupActions = entries,
    )
}
