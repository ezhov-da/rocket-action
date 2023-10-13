package ru.ezhov.rocket.action.application.event.domain;

interface DomainEventSubscriber<T : DomainEvent> {
    fun handleEvent(event: T)

    fun subscribedToEventType(): Class<T>
}
