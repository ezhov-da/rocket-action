package ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event

import ru.ezhov.rocket.action.application.event.domain.DomainEvent

data class ActionSchedulerCreatedDomainEvent(
    val actionId: String
) : DomainEvent
