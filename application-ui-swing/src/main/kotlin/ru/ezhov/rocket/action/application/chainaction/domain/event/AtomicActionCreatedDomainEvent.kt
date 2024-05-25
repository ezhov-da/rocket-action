package ru.ezhov.rocket.action.application.chainaction.domain.event

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.event.domain.DomainEvent

data class AtomicActionCreatedDomainEvent(
    val atomicAction: AtomicAction
) : DomainEvent
