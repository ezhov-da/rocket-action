package ru.ezhov.rocket.action.application.configuration.ui.event

import ru.ezhov.rocket.action.application.configuration.ui.event.model.ConfigurationUiEvent

interface ConfigurationUiListener {
    fun action(event: ConfigurationUiEvent)
}