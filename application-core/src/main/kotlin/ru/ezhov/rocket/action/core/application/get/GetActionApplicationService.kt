package ru.ezhov.rocket.action.core.application.get

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId

interface GetActionApplicationService {
    fun children(id: ActionId): Either<GetActionApplicationServiceException, List<Action>>
    fun action(id: ActionId): Either<GetActionApplicationServiceException,Action?>
}
