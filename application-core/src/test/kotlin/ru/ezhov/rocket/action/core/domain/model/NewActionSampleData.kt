package ru.ezhov.rocket.action.core.domain.model

import java.time.LocalDateTime


object NewActionSampleData {
    fun default(
        id: ActionId = ActionIdSampleData.new(),
        type: ActionType = ActionType("test"),
        order: ActionOrder = ActionOrder(1),
        creationDate: LocalDateTime = LocalDateTime.now(),
        updateDate: LocalDateTime? = null,
        parentId: ActionId? = null,
        map: Map<ActionSettingName, ActionSettingValue> = emptyMap(),
    ) = NewAction(
        id = id,
        type = type,
        order = order,
        creationDate = creationDate,
        updateDate = updateDate,
        parentId = parentId,
        map = map,
    )
}
