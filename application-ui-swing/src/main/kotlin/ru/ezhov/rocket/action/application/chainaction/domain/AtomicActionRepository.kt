package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction

interface AtomicActionRepository {
    fun save(atomicAction: AtomicAction)

    fun all(): List<AtomicAction>

    fun byId(id: String): AtomicAction?

    fun delete(id: String)
}
