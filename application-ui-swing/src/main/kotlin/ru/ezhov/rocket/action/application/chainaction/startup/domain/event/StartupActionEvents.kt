package ru.ezhov.rocket.action.application.chainaction.startup.domain.event

import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunStatus
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger
import ru.ezhov.rocket.action.application.event.domain.DomainEvent

/**
 * The saved startup list has changed (saved, or entries were removed with a deleted action).
 */
class StartupActionsUpdatedDomainEvent : DomainEvent {
    override fun toString(): String = "StartupActionsUpdatedDomainEvent"
}

class StartupActionRunStartedDomainEvent(
    val trigger: StartupRunTrigger,
    val total: Int,
) : DomainEvent {
    override fun toString(): String = "StartupActionRunStartedDomainEvent(trigger=$trigger, total=$total)"
}

class StartupActionEntryStartedDomainEvent(
    val entryId: String,
    val index: Int,
    val total: Int,
) : DomainEvent {
    override fun toString(): String =
        "StartupActionEntryStartedDomainEvent(entryId=$entryId, index=$index, total=$total)"
}

class StartupActionEntryFinishedDomainEvent(
    val entryId: String,
    val status: StartupRunStatus,
    val error: String? = null,
) : DomainEvent {
    override fun toString(): String =
        "StartupActionEntryFinishedDomainEvent(entryId=$entryId, status=$status)"
}

class StartupActionRunFinishedDomainEvent(
    val result: StartupRunResult,
) : DomainEvent {
    override fun toString(): String = "StartupActionRunFinishedDomainEvent(trigger=${result.trigger})"
}
