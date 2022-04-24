package ru.ezhov.rocket.action.application.new_.domain.repository

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.Action
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings
import ru.ezhov.rocket.action.application.new_.domain.model.NewAction

interface ActionAndSettingsRepository {
    fun add(action: NewAction): Either<AddActionAndSettingsRepositoryException, Unit>

    fun remove(id: ActionId, withAllChildrenRecursive: Boolean): Either<RemoveActionAndSettingsRepositoryException, Unit>
}
