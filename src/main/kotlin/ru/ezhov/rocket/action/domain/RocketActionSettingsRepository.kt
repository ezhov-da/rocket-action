package ru.ezhov.rocket.action.domain

import ru.ezhov.rocket.action.api.RocketActionSettings

interface RocketActionSettingsRepository {
    @Throws(RocketActionSettingsRepositoryException::class)
    fun actions(): List<RocketActionSettings>

    @Throws(RocketActionSettingsRepositoryException::class)
    fun save(settings: List<RocketActionSettings>)
}