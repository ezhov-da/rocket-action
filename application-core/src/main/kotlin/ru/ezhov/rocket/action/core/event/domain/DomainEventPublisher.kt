package ru.ezhov.rocket.action.core.event.domain;

import java.util.List;

interface DomainEventPublisher<T : DomainEvent> {
    fun publish(events: List<T>)


}
