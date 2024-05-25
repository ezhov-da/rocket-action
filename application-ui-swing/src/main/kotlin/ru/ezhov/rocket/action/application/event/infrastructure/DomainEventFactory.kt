package ru.ezhov.rocket.action.application.event.infrastructure

import ru.ezhov.rocket.action.application.event.domain.DomainEventPublisher
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriberRegistrar

object DomainEventFactory {
    private val INSTANCE = InMemoryDomainEventPublisher()

    val publisher: DomainEventPublisher = INSTANCE

    val subscriberRegistrar: DomainEventSubscriberRegistrar = INSTANCE
}
