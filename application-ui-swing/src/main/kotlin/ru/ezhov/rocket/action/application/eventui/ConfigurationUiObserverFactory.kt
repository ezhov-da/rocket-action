package ru.ezhov.rocket.action.application.eventui

object ConfigurationUiObserverFactory {
    val observer: ConfigurationUiObserver = InMemoryConfigurationUiObserver()
}
