package ru.ezhov.rocket.action.core.event.domain;

interface DomainEventSubscriber<T : DomainEvent> {
    fun handleEvent(event: T)

    fun subscribedToEventType(): Class<T>
}
