package ru.ezhov.rocket.action.application.chainaction.application

import org.springframework.beans.factory.InitializingBean
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
) : InitializingBean {
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

    fun byAlias(alias: String): AtomicAction? = atomics().firstOrNull() { it.alias == alias }

    /**
     * Used only for access outside the Spring context.
     * Important! Can be null if called before the context is initialized.
     */
    companion object {
        var INSTANCE: AtomicActionService? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = this
    }
}
