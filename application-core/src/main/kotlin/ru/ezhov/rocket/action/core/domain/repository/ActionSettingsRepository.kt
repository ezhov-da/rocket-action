package ru.ezhov.rocket.action.core.domain.repository

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.ActionSettings

interface ActionSettingsRepository {
    fun all(): Either<AllActionSettingsRepositoryException, List<ActionSettings>>

    fun settings(id: ActionId): Either<ActionSettingsRepositoryException, ActionSettings?>

    fun save(actionSettings: ActionSettings): Either<SaveActionSettingsRepositoryException, Unit>
}
