package ru.ezhov.rocket.action.application.chainaction.domain.model

import java.util.*

data class ChainAction(
    var id: String,
    var name: String,
    var description: String,
    var actions: List<ActionOrder>,
) : Action {
    override fun id(): String = id

    fun duplicate(): ChainAction = ChainAction(
        id = UUID.randomUUID().toString(),
        name = this.name,
        description = this.name,
        actions = this.actions,
    )
}

/**
 * Due to the fact that the same action can be reused in the same chain,
 * it is necessary to have unique action identifiers for a specific chain
 */
data class ActionOrder(
    /**
     * Unique ID
     */
    val chainOrderId: String,
    val actionId: String,
)
