package ru.ezhov.rocket.action.application.event.infrastructure;

import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventPublisher
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriberRegistrar
import java.util.concurrent.ConcurrentHashMap

class InMemoryDomainEventPublisher<T : DomainEvent> :
    DomainEventPublisher<T>,
    DomainEventSubscriberRegistrar<T> {
    private val eventSubscribers: MutableMap<Class<T>, DomainEventSubscriber<T>> = ConcurrentHashMap()

    @SuppressWarnings("unchecked")
    override fun publish(events: List<T>) {
        events.forEach { e ->
            eventSubscribers[e.javaClass]?.handleEvent(e)
        }
    }

    override fun subscribe(domainEventSubscriber: DomainEventSubscriber<T>) {
        eventSubscribers[domainEventSubscriber.subscribedToEventType()] = domainEventSubscriber
    }

    override fun unsubscribe(domainEventSubscriber: DomainEventSubscriber<T>) {
        eventSubscribers.remove(domainEventSubscriber.subscribedToEventType())
    }
}
