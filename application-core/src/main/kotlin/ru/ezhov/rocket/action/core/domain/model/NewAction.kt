package ru.ezhov.rocket.action.core.domain.model

import java.time.LocalDateTime

class NewAction private constructor(
    val id: ActionId,
    val type: ActionType,
    val order: NewActionOrder,
    val creationDate: LocalDateTime,
    val parentId: ActionId?,
    val map: Map<ActionSettingName, ActionSettingValue?>,
) {
    companion object {
        fun create(
            id: ActionId,
            type: ActionType,
            order: NewActionOrder,
            creationDate: LocalDateTime,
            parentId: ActionId?,
            map: Map<ActionSettingName, ActionSettingValue?>,
        ) = NewAction(
            id = id,
            type = type,
            order = order,
            creationDate = creationDate,
            parentId = parentId,
            map = map,
        )
    }

    fun toAction() = Action.create(
        id = id,
        type = type,
        order = ActionOrder(order.order()),
        creationDate = creationDate,
        parentId = parentId,
    )

    fun toActionSettings() = ActionSettings(
        id = id,
        map = map,
    )
}
