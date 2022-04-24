package ru.ezhov.rocket.action.core.domain.model

import java.time.LocalDateTime

class NewAction(
    val id: ActionId,
    val type: ActionType,
    val order: ActionOrder,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
    val parentId: ActionId?,
    val map: Map<ActionSettingName, ActionSettingValue>,
) {
}
