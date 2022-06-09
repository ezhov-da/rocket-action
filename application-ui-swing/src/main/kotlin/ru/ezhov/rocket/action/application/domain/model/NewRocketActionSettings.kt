package ru.ezhov.rocket.action.application.domain.model

import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.NewActionOrder

data class NewRocketActionSettings(
    val parentId: String?,
    val order: Order,
    val type: String,
    val properties: Map<String, String>
)

data class Order private constructor(
    val afterId: String? = null,
    val beforeId: String? = null,
    val orderRelation: Int,
) {
    companion object {
        fun empty() = Order(orderRelation = 1)

        fun after(id: String, orderRelation: Int) = Order(afterId = id, orderRelation = orderRelation)

        fun before(id: String, orderRelation: Int) = Order(beforeId = id, orderRelation = orderRelation)
    }

    private fun hasAfterOrBefore() = afterId != null && beforeId != null

    private fun isAfter(): Boolean = afterId == null

    fun to(): NewActionOrder = if (hasAfterOrBefore()) {
        if (isAfter()) {
            NewActionOrder.after(id = ActionId.of(afterId!!), orderRelation = orderRelation)
        } else {
            NewActionOrder.before(id = ActionId.of(beforeId!!), orderRelation = orderRelation)
        }
    } else {
        NewActionOrder.empty()
    }
}
