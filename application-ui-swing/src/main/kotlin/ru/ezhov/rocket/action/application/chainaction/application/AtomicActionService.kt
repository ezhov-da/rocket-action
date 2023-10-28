package ru.ezhov.rocket.action.application.chainaction.application

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.domain.AtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory

@Service
class AtomicActionService(
    private val atomicActionRepository: AtomicActionRepository,
) {
    fun atomics(): List<AtomicAction> = atomicActionRepository.all()

    fun atomicBy(id: String): AtomicAction? = atomicActionRepository.byId(id)

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
