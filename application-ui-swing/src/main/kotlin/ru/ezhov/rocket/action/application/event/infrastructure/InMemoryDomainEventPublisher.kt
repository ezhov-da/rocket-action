package ru.ezhov.rocket.action.application.event.infrastructure;

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventPublisher
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriberRegistrar
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class InMemoryDomainEventPublisher :
    DomainEventPublisher,
    DomainEventSubscriberRegistrar {
    private val eventSubscribers: MutableMap<Class<*>, MutableSet<DomainEventSubscriber>> =
        ConcurrentHashMap()

    @SuppressWarnings("unchecked")
    override fun publish(events: List<DomainEvent>) {
        logger.debug { "Publish events '${events.map { it::class.java.name }}'" }

        events.forEach { e ->
            eventSubscribers[e.javaClass]?.forEach { subscriber ->
                subscriber.handleEvent(e)
            }
        }
    }

    override fun subscribe(domainEventSubscriber: DomainEventSubscriber) {
        logger.debug { "Subscribe '${domainEventSubscriber::class.java.name}' to ${domainEventSubscriber.subscribedToEventType()}" }

        domainEventSubscriber.subscribedToEventType().forEach { eventType ->
            val set = eventSubscribers.getOrPut(eventType) { mutableSetOf() }
            set.add(domainEventSubscriber)
        }
    }

    override fun unsubscribe(domainEventSubscriber: DomainEventSubscriber) {
        logger.debug { "Unsubscribe '${domainEventSubscriber::class.java.name}' from ${domainEventSubscriber.subscribedToEventType()}" }

        domainEventSubscriber.subscribedToEventType().forEach { eventType ->
            eventSubscribers[eventType]?.remove(domainEventSubscriber)
        }
    }
}
