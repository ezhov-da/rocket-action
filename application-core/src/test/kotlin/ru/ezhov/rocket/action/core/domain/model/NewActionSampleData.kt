package ru.ezhov.rocket.action.core.domain.model

import java.time.LocalDateTime


object NewActionSampleData {
    fun default(
        id: ActionId = ActionIdSampleData.new(),
        type: ActionType = ActionType("test"),
        order: ActionOrder = ActionOrder(1),
        creationDate: LocalDateTime = LocalDateTime.now(),
        parentId: ActionId? = null,
        map: Map<ActionSettingName, ActionSettingValue> = emptyMap(),
    ) = NewAction.create(
        id = id,
        type = type,
        order = order,
        creationDate = creationDate,
        parentId = parentId,
        map = map,
    )
}
