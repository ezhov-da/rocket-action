package ru.ezhov.rocket.action.application.chainaction.domain.model

class ChainAction(
    var id: String,
    var name: String,
    var description: String,
    var actionIds: List<String>
)
