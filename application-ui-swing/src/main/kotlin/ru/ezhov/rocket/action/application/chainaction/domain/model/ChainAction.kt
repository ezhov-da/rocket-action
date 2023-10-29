package ru.ezhov.rocket.action.application.chainaction.domain.model

class ChainAction(
    var id: String,
    var name: String,
    var description: String,
    var actions: List<ActionOrder>,
)

/**
 * Due to the fact that the same action can be reused in the same chain,
 * it is necessary to have unique action identifiers for a specific chain
 */
data class ActionOrder(
    val chainOrderId: String,
    val actionId: String,
)
