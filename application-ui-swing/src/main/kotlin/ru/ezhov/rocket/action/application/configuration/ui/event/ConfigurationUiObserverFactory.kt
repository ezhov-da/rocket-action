package ru.ezhov.rocket.action.application.configuration.ui.event

object ConfigurationUiObserverFactory {
    val observer: ConfigurationUiObserver = InMemoryConfigurationUiObserver()
}