package ru.ezhov.rocket.action.application.eventui

import ru.ezhov.rocket.action.application.eventui.model.ConfigurationUiEvent

interface ConfigurationUiListener {
    fun action(event: ConfigurationUiEvent)
}
