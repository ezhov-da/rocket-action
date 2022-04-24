package ru.ezhov.rocket.action.application.configuration.ui.event

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.configuration.ui.event.model.ConfigurationUiEvent
import java.util.concurrent.ConcurrentLinkedDeque

private val logger = KotlinLogging.logger { }

class InMemoryConfigurationUiObserver : ConfigurationUiObserver {
    private val listeners: ConcurrentLinkedDeque<ConfigurationUiListener> = ConcurrentLinkedDeque()
    override fun notify(event: ConfigurationUiEvent) {
        listeners
            .forEach { listener ->
                try {
                    listener.action(event)

                    logger.debug {
                        "notify listener='${listener::class.java.simpleName}' on " +
                            "event='${event::class.simpleName}' success"
                    }
                } catch (ex: Exception) {
                    logger.error(ex) {
                        "error when notify listener='${listener::class.java.simpleName}' " +
                            "on event='${event::class.simpleName}' "
                    }
                }
            }
    }

    override fun register(listener: ConfigurationUiListener) {
        listeners.add(listener)
        logger.debug { "register listener='${listener::class.java.simpleName}'. Listeners count=${listeners.size}" }
    }

    override fun remove(listener: ConfigurationUiListener) {
        listeners.remove(listener)
        logger.debug { "remove listener='${listener::class.java.simpleName}'. Listeners count=${listeners.size}" }
    }
}