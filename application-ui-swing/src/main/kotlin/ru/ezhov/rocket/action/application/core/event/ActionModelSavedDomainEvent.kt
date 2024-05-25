package ru.ezhov.rocket.action.application.core.event

import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.event.domain.DomainEvent

data class ActionModelSavedDomainEvent(
    /**
     * It is forbidden to change, only for information
     */
    val actionsModel: ActionsModel,
) : DomainEvent
