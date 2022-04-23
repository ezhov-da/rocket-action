package ru.ezhov.rocket.action.application.new_.domain

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings

interface ActionSettingsRepository {
    fun all(): Either<AllActionSettingsRepositoryException, List<ActionSettings>>

    fun settings(id: ActionId): Either<ActionSettingsRepositoryException, ActionSettings?>
}
