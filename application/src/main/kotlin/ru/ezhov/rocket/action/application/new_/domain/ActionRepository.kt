package ru.ezhov.rocket.action.application.new_.domain

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.Action
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface ActionRepository {
    fun action(id: ActionId): Either<GetActionRepositoryException, Action?>

    fun actions(): Either<GetActionRepositoryException, List<Action>>

    fun save(action: Action): Either<GetActionRepositoryException, Unit>

    fun children(id: ActionId): Either<GetActionRepositoryException, List<Action>>
}
