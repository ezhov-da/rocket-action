package ru.ezhov.rocket.action.application.chainaction.domain.event

import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.event.domain.DomainEvent

data class ChainActionUpdatedDomainEvent(
    val chainAction: ChainAction
): DomainEvent
