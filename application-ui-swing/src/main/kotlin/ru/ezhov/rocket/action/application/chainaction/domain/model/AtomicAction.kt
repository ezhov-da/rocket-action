package ru.ezhov.rocket.action.application.chainaction.domain.model

data class AtomicAction(
    var id: String,
    var name: String,
    var description: String,
    var engine: AtomicActionEngine,
    var source: AtomicActionSource,
    var data: String,
)

enum class AtomicActionSource {
    FILE,
    TEXT,
}

enum class AtomicActionEngine {
    GROOVY,
    KOTLIN,
}
