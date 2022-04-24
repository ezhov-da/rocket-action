package ru.ezhov.rocket.action.application.new_.event.infrastructure;

import ru.ezhov.rocket.action.application.new_.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.new_.event.domain.DomainEventPublisher
import ru.ezhov.rocket.action.application.new_.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.new_.event.domain.DomainEventSubscriberRegistrar
import java.util.List
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

    override fun subscribe(eventSubscriber: DomainEventSubscriber<T>) {
        eventSubscribers[eventSubscriber.subscribedToEventType()] = eventSubscriber
    }

    override fun unsubscribe(eventSubscriber: DomainEventSubscriber<T>) {
        eventSubscribers.remove(eventSubscriber.subscribedToEventType())
    }
}
