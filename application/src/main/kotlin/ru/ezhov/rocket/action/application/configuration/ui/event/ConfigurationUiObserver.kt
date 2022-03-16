package ru.ezhov.rocket.action.application.configuration.ui.event

import ru.ezhov.rocket.action.application.configuration.ui.event.model.ConfigurationUiEvent

interface ConfigurationUiObserver {
    fun notify(event: ConfigurationUiEvent)

    fun register(listener: ConfigurationUiListener)

    fun remove(listener: ConfigurationUiListener)
}