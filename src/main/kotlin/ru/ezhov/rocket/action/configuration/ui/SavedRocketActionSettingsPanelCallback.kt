package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionSettings

interface SavedRocketActionSettingsPanelCallback {
    fun saved(rocketActionSettings: RocketActionSettings)
}