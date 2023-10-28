package ru.ezhov.rocket.action.application.chainaction.application

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.domain.AtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory

@Service
class ChainActionService(
    private val chainActionRepository: ChainActionRepository,
    private val atomicActionRepository: AtomicActionRepository,
) {
    fun chains(): List<ChainAction> = chainActionRepository.all()

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

    fun atomics(): List<AtomicAction> = atomicActionRepository.all()

    fun deleteAtomic(id: String) {
        atomicActionRepository.delete(id)

        DomainEventFactory.publisher.publish(listOf(AtomicActionDeletedDomainEvent(id)))
    }

    fun addAtomic(atomicAction: AtomicAction) {
        atomicActionRepository.save(atomicAction)

        DomainEventFactory.publisher.publish(listOf(AtomicActionCreatedDomainEvent(atomicAction)))
    }

    fun updateAtomic(atomicAction: AtomicAction) {
        atomicActionRepository.save(atomicAction)

        DomainEventFactory.publisher.publish(listOf(AtomicActionUpdatedDomainEvent(atomicAction)))
    }
}
