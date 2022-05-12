package ru.ezhov.rocket.action.application.domain

import ru.ezhov.rocket.action.api.RocketActionSettings

interface RocketActionSettingsRepository {
    fun actions(): List<RocketActionSettings>

    fun save(settings: List<RocketActionSettings>)
}
