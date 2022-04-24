package ru.ezhov.rocket.action.application.new_.application.get

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.Action
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface GetActionApplicationService {
    fun children(id: ActionId): Either<GetActionApplicationServiceException, List<Action>>
    fun action(id: ActionId): Either<GetActionApplicationServiceException,Action?>
}
