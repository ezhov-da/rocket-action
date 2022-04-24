package ru.ezhov.rocket.action.application.new_.event.domain;

import java.util.List;

interface DomainEventPublisher<T : DomainEvent> {
    fun publish(events: List<T>)


}
