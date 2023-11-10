package ru.ezhov.rocket.action.application.chainaction.domain.model

data class AtomicAction(
    var id: String,
    var name: String,
    var description: String,
    var contractType: ContractType,
    var engine: AtomicActionEngine,
    var source: AtomicActionSource,
    var data: String,
) : Action {
    override fun id(): String = id
}

enum class ContractType(val inputValue: InputValueContractType, val output: OutputValueContractType) {
    IN_OUT(InputValueContractType.IN, OutputValueContractType.OUT),
    IN_UNIT(InputValueContractType.IN, OutputValueContractType.UNIT),
    UNIT_OUT(InputValueContractType.UNIT, OutputValueContractType.OUT),
    UNIT_UNIT(InputValueContractType.UNIT, OutputValueContractType.UNIT),
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
    KOTLIN,
}
