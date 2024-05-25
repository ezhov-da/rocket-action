package ru.ezhov.rocket.action.application.eventui

import ru.ezhov.rocket.action.application.eventui.model.ConfigurationUiEvent

interface ConfigurationUiObserver {
    fun notify(event: ConfigurationUiEvent)

    fun register(listener: ConfigurationUiListener)

    fun remove(listener: ConfigurationUiListener)
}
