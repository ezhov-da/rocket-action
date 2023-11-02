package ru.ezhov.rocket.action.application.chainaction.domain.model

data class AtomicAction(
    var id: String,
    var name: String,
    var description: String,
    var contractType: ContractType,
    var engine: AtomicActionEngine,
    var source: AtomicActionSource,
    var data: String,
): Action{
    override fun id(): String = id
}

enum class ContractType {
    IN_OUT,
    IN_UNIT,
    UNIT_OUT,
    UNIT_UNIT,
}

enum class AtomicActionSource {
    FILE,
    TEXT,
}

enum class AtomicActionEngine {
    GROOVY,
    KOTLIN,
}
