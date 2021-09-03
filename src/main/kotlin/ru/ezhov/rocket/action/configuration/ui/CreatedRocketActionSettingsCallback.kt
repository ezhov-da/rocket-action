package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionSettings

interface CreatedRocketActionSettingsCallback {
    fun create(rocketActionSettings: RocketActionSettings)
}