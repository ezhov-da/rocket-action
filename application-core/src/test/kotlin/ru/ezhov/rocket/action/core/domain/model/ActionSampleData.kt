package ru.ezhov.rocket.action.core.domain.model

import java.time.LocalDateTime

object ActionSampleData {
    fun default(
        id: ActionId = ActionIdSampleData.default(),
        type: ActionType = ActionType("COPY_TO_CLIPBOARD"),
        order: ActionOrder = ActionOrder(1),
        creationDate: LocalDateTime = LocalDateTime.now(),
        updateDate: LocalDateTime? = null,
        parentId: ActionId? = null,
    ): Action = Action.create(
        id = id,
        type = type,
        order = order,
        creationDate = creationDate,
        updateDate = updateDate,
        parentId = parentId,
    )
}
