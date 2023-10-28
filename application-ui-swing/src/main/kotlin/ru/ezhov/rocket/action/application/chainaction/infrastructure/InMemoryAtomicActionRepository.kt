package ru.ezhov.rocket.action.application.chainaction.infrastructure

import org.springframework.stereotype.Repository
import ru.ezhov.rocket.action.application.chainaction.domain.AtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction

@Repository
class InMemoryAtomicActionRepository : AtomicActionRepository {
    private val actions: MutableList<AtomicAction> = mutableListOf()

    override fun save(atomicAction: AtomicAction) {
        val index = actions.indexOfFirst { aa -> aa.id == atomicAction.id }
        if (index == -1) {
            actions.add(atomicAction)
        } else {
            actions.add(index, atomicAction)
        }
    }

    override fun all(): List<AtomicAction> = actions.toList()

    override fun delete(id: String) {
        val index = actions.indexOfFirst { aa -> aa.id == id }
        if (index != -1) {
            actions.removeAt(index)
        }
    }
}
