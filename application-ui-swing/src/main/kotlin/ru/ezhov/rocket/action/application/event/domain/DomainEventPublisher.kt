package ru.ezhov.rocket.action.application.event.domain;

interface DomainEventPublisher {
    fun publish(events: List<DomainEvent>)
}
