package ru.ezhov.rocket.action.application.new_.domain.changeorder

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface AfterChangeOrderService {
    fun after(target: ActionId, after: ActionId): Either<ChangeOrderServiceException, Unit>
}
