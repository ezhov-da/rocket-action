package ru.ezhov.rocket.action.core.domain.repository

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId

interface ActionRepository {
    fun action(id: ActionId): Either<GetActionRepositoryException, Action?>

    fun actions(ids: List<ActionId>): Either<GetActionRepositoryException, List<Action>>

    fun all(): Either<GetActionRepositoryException, List<Action>>

    fun addOrUpdate(actions: List<Action>): Either<SaveActionRepositoryException, Unit>

    fun children(id: ActionId?): Either<GetChildrenActionRepositoryException, List<Action>>

    fun remove(ids: List<ActionId>): Either<RemoveActionRepositoryException, Unit>
}
