package ru.ezhov.rocket.action.application.new_.event.domain

interface DomainEventSubscriberRegistrar<T : DomainEvent> {
    fun subscribe(domainEventSubscriber: DomainEventSubscriber<T>)

    fun unsubscribe(domainEventSubscriber: DomainEventSubscriber<T>)
}
