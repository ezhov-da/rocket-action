package ru.ezhov.rocket.action.application.chainaction.domain.model

interface Action {
    fun id(): String

    fun name(): String

    fun description(): String

    fun icon(): String?

    /**
     * Custom name under which the action is published as an MCP tool, if set.
     */
    fun mcpToolName(): String? = null
}
