package ru.ezhov.rocket.action.application.event.domain;

interface DomainEventPublisher<T : DomainEvent> {
    fun publish(events: List<T>)
}
