package ru.ezhov.rocket.action.core.domain.model

import java.time.LocalDateTime

class NewAction private constructor(
    val id: ActionId,
    val type: ActionType,
    val order: ActionOrder,
    val creationDate: LocalDateTime,
    val parentId: ActionId?,
    val map: Map<ActionSettingName, ActionSettingValue?>,
) {
    companion object {
        fun create(
            id: ActionId,
            type: ActionType,
            order: ActionOrder,
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
}
