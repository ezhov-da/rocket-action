package ru.ezhov.rocket.action.application.new_.domain.model

import java.time.LocalDateTime

class Action private constructor(
    val id: ActionId,
    val type: ActionType,
    val order: ActionOrder,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
    val parentId: ActionId?,
) {
    companion object {
        fun restore(
            id: ActionId,
            type: ActionType,
            order: ActionOrder,
            creationDate: LocalDateTime,
            updateDate: LocalDateTime?,
            parentId: ActionId?,
        ) = Action(
            id = id,
            type = type,
            order = order,
            creationDate = creationDate,
            updateDate = updateDate,
            parentId = parentId,
        )
    }
}
