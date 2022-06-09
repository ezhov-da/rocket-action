package ru.ezhov.rocket.action.application.domain

import arrow.core.Either
import ru.ezhov.rocket.action.application.domain.model.NewRocketActionSettings
import ru.ezhov.rocket.action.application.infrastructure.RocketActionSettingsNode
import ru.ezhov.rocket.action.core.domain.model.ActionId

interface ConfigRocketActionSettingsRepository {
    fun actions(): List<RocketActionSettingsNode>


    fun update(settings: NewRocketActionSettings): Either<RocketActionSettingsRepositoryException, Unit>

    fun create(settings: NewRocketActionSettings): Either<RocketActionSettingsRepositoryException, Unit>

    fun delete(id: ActionId): Either<RocketActionSettingsRepositoryException, Unit>

    fun before(id: ActionId, beforeId: ActionId): Either<RocketActionSettingsRepositoryException, Unit>

    fun after(id: ActionId, afterId: ActionId): Either<RocketActionSettingsRepositoryException, Unit>
}
