package ru.ezhov.rocket.action.application.domain

import arrow.core.Either
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.application.infrastructure.RocketActionSettingsNode
import ru.ezhov.rocket.action.core.domain.model.ActionId

interface ConfigRocketActionSettingsRepository {
    fun actions(): List<RocketActionSettingsNode>


    fun update(settings: RocketActionSettings, afterId: String? = null): Either<RocketActionSettingsRepositoryException, Unit>

    fun create(settings: RocketActionSettings, afterId: String? = null): Either<RocketActionSettingsRepositoryException, Unit>

    fun delete(id: ActionId): Either<RocketActionSettingsRepositoryException, Unit>

    fun before(id: ActionId, beforeId: ActionId): Either<RocketActionSettingsRepositoryException, Unit>

    fun after(id: ActionId, afterId: ActionId): Either<RocketActionSettingsRepositoryException, Unit>
}
