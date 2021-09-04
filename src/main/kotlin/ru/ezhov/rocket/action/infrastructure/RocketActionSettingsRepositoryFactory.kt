package ru.ezhov.rocket.action.infrastructure

import ru.ezhov.rocket.action.domain.RocketActionSettingsRepository
import java.net.URI

object RocketActionSettingsRepositoryFactory {
    fun repository(uri: URI): RocketActionSettingsRepository = YmlRocketActionSettingsRepository(uri)
}