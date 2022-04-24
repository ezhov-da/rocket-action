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
    fun withNewOrder(order: ActionOrder): Action =
        Action(
            id = id,
            type = type,
            order = order,
            creationDate = creationDate,
            updateDate = updateDate,
            parentId = parentId,
        )

    fun withNewParent(parentId: ActionId?): Action =
        Action(
            id = id,
            type = type,
            order = order,
            creationDate = creationDate,
            updateDate = updateDate,
            parentId = parentId,
        )

    companion object {
        fun create(
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
