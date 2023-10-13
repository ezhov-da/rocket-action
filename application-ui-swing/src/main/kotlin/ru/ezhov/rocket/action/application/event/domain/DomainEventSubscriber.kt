package ru.ezhov.rocket.action.application.event.domain;

interface   DomainEventSubscriber {
    fun handleEvent(event: DomainEvent)

    fun subscribedToEventType(): Class<*>
}
