package ru.ezhov.rocket.action.configuration.ui.event

object ConfigurationUiObserverFactory {
    val observer: ConfigurationUiObserver = InMemoryConfigurationUiObserver()
}