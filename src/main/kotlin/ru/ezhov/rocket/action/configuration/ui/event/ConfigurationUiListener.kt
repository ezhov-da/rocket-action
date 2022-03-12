package ru.ezhov.rocket.action.configuration.ui.event

import ru.ezhov.rocket.action.configuration.ui.event.model.ConfigurationUiEvent

interface ConfigurationUiListener {
    fun action(event: ConfigurationUiEvent)
}