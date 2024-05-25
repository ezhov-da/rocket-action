package ru.ezhov.rocket.action.application.chainaction.application

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory

@Service
class ChainActionService(
    private val chainActionRepository: ChainActionRepository,
) : InitializingBean {
    fun chains(): List<ChainAction> = chainActionRepository.all()

    fun byId(id: String): ChainAction? = chainActionRepository.all().firstOrNull { it.id == id }

    fun deleteChain(id: String) {
        chainActionRepository.delete(id)

        DomainEventFactory.publisher.publish(listOf(ChainActionDeletedDomainEvent(id)))
    }

    fun addChain(chainAction: ChainAction) {
        chainActionRepository.save(chainAction)

        DomainEventFactory.publisher.publish(listOf(ChainActionCreatedDomainEvent(chainAction)))
    }

    fun updateChain(chainAction: ChainAction) {
        chainActionRepository.save(chainAction)

        DomainEventFactory.publisher.publish(listOf(ChainActionUpdatedDomainEvent(chainAction)))
    }

    fun usageAction(id: String) = chains().filter { it.actions.any { act -> act.actionId == id } }

    /**
     * Used only for access outside the Spring context.
     * Important! Can be null if called before the context is initialized.
     */
    companion object {
        var INSTANCE: ChainActionService? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = this
    }
}
