package ru.ezhov.rocket.action.application.configuration.ui.create

import ru.ezhov.rocket.action.application.configuration.ui.create.model.CreateRocketAction

interface CreatedRocketActionSettingsCallback {
    fun create(action: CreateRocketAction)
}
