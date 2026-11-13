package ru.ezhov.rocket.action.application.chainaction.domain.model

import java.util.*

data class AtomicAction(
    var id: String,
    var name: String,
    var description: String,
    var contractType: ContractType,
    var engine: AtomicActionEngine,
    var source: AtomicActionSource,
    var data: String,
    var alias: String? = null,
    var icon: String? = null,
    /**
     * Publish this atomic action as an MCP tool.
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

    fun duplicate(): AtomicAction = AtomicAction(
        id = UUID.randomUUID().toString(),
        name = this.name,
        description = this.description,
        contractType = this.contractType,
        engine = this.engine,
        source = this.source,
        data = this.data,
        alias = this.alias,
        isTool = this.isTool,
        mcpToolName = this.mcpToolName,
    )
}

enum class ContractType(val inputValue: InputValueContractType, val output: OutputValueContractType) {
    IN_OUT(InputValueContractType.IN, OutputValueContractType.OUT),
    IN_UNIT(InputValueContractType.IN, OutputValueContractType.UNIT),
    UNIT_OUT(InputValueContractType.UNIT, OutputValueContractType.OUT),
    UNIT_UNIT(InputValueContractType.UNIT, OutputValueContractType.UNIT),
    ;

    fun isUnitInputContract(): Boolean =
        this == UNIT_OUT ||
            this == UNIT_UNIT
}

enum class InputValueContractType {
    IN,
    UNIT
}

enum class OutputValueContractType {
    OUT,
    UNIT
}

enum class AtomicActionSource {
    FILE,
    TEXT,
}

enum class AtomicActionEngine {
    GROOVY,
}
