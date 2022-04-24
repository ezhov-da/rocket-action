package ru.ezhov.rocket.action.core.domain.repository

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.ActionSettings
import ru.ezhov.rocket.action.core.domain.model.NewAction

interface ActionAndSettingsRepository {
    fun add(action: NewAction): Either<AddActionAndSettingsRepositoryException, Unit>

    fun remove(id: ActionId, withAllChildrenRecursive: Boolean): Either<RemoveActionAndSettingsRepositoryException, Unit>
}
