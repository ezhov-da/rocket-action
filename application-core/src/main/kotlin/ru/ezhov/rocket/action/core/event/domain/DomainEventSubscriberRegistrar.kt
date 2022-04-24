package ru.ezhov.rocket.action.core.event.domain

interface DomainEventSubscriberRegistrar<T : DomainEvent> {
    fun subscribe(domainEventSubscriber: DomainEventSubscriber<T>)

    fun unsubscribe(domainEventSubscriber: DomainEventSubscriber<T>)
}
