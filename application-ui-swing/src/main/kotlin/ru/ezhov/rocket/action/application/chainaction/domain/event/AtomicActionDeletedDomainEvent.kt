package ru.ezhov.rocket.action.application.chainaction.domain.event

import ru.ezhov.rocket.action.application.event.domain.DomainEvent

data class AtomicActionDeletedDomainEvent(
    val id: String
) : DomainEvent
