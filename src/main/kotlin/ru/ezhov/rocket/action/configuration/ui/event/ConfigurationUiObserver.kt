package ru.ezhov.rocket.action.configuration.ui.event

import ru.ezhov.rocket.action.configuration.ui.event.model.ConfigurationUiEvent

interface ConfigurationUiObserver {
    fun notify(event: ConfigurationUiEvent)

    fun register(listener: ConfigurationUiListener)

    fun remove(listener: ConfigurationUiListener)
}