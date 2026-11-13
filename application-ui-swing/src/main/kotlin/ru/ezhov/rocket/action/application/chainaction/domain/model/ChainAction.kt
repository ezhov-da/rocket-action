package ru.ezhov.rocket.action.application.chainaction.domain.model

import java.util.*

data class ChainAction(
    var id: String,
    var name: String,
    var description: String,
    var actions: List<ActionOrder>,
    var icon: String? = null,
    /**
     * Publish this chain action as an MCP tool.
     */
    var isTool: Boolean = false,
    /**
     * Custom name under which the action is published as an MCP tool.
     * Null or blank means the name is generated from [name] and [id].
     */
    var mcpToolName: String? = null,
) : Action {
    override fun id(): String = id

    override fun name(): String = name

    override fun description(): String = description

    override fun icon(): String? = icon

    override fun mcpToolName(): String? = this.mcpToolName

    fun duplicate(): ChainAction = ChainAction(
        id = UUID.randomUUID().toString(),
        name = this.name,
        description = this.name,
        actions = this.actions,
        isTool = this.isTool,
        mcpToolName = this.mcpToolName,
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
