package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction

interface ChainActionRepository {
    fun save(chainAction: ChainAction)

    fun all(): List<ChainAction>

    fun delete(id: String)
}
