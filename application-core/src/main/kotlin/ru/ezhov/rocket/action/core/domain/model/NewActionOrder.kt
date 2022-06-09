package ru.ezhov.rocket.action.core.domain.model

data class NewActionOrder private constructor(
    val afterId: ActionId? = null,
    val beforeId: ActionId? = null,
    val orderRelation: Int,
) {
    companion object {
        fun after(id: ActionId, orderRelation: Int) = NewActionOrder(afterId = id, orderRelation = orderRelation)

        fun before(id: ActionId, orderRelation: Int) = NewActionOrder(beforeId = id, orderRelation = orderRelation)

        fun empty() = NewActionOrder(orderRelation = 1)
    }

    fun isAfter(): Boolean = afterId == null

    fun hasAfterOrBefore() = afterId != null && beforeId != null

    fun order() = if (hasAfterOrBefore()) {
        if (isAfter()) orderRelation + 1 else maxOf(orderRelation - 1, 1)
    } else {
        1
    }
}
