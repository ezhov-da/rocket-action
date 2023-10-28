package ru.ezhov.rocket.action.application.chainaction.domain.event

import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.event.domain.DomainEvent

data class ChainActionDeletedDomainEvent(
    val id: String
): DomainEvent
