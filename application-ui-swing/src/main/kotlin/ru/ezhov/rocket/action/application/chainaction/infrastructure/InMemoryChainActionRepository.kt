package ru.ezhov.rocket.action.application.chainaction.infrastructure

import org.springframework.stereotype.Repository
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction

@Repository
class InMemoryChainActionRepository : ChainActionRepository {
    private val chains: MutableList<ChainAction> = mutableListOf()

    override fun save(chainAction: ChainAction) {
        val index = chains.indexOfFirst { aa -> aa.id == chainAction.id }
        if (index == -1) {
            chains.add(chainAction)
        } else {
            chains.add(index, chainAction)
        }
    }

    override fun all(): List<ChainAction> = chains.toList()

    override fun delete(id: String) {
        val index = chains.indexOfFirst { aa -> aa.id == id }
        if (index != -1) {
            chains.removeAt(index)
        }
    }
}
