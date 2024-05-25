package ru.ezhov.rocket.action.application.event.domain

interface DomainEventSubscriberRegistrar {
    fun subscribe(domainEventSubscriber: DomainEventSubscriber)

    fun unsubscribe(domainEventSubscriber: DomainEventSubscriber)
}
