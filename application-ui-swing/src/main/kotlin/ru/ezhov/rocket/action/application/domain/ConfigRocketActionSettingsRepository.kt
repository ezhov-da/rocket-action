package ru.ezhov.rocket.action.application.domain

import arrow.core.Either
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.application.infrastructure.RocketActionSettingsNode

interface ConfigRocketActionSettingsRepository {
    fun actions(): List<RocketActionSettingsNode>

    fun create(settings: RocketActionSettings, afterId: String? = null): Either<RocketActionSettingsRepositoryException, Unit>

    fun delete(id: String): Either<RocketActionSettingsRepositoryException, Unit>

    fun before(id: String, beforeId: String): Either<RocketActionSettingsRepositoryException, Unit>

    fun after(id: String, afterId: String): Either<RocketActionSettingsRepositoryException, Unit>
}
