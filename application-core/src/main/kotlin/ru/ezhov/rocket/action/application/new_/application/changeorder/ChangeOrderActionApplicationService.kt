package ru.ezhov.rocket.action.application.new_.application.changeorder

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface ChangeOrderActionApplicationService {
    fun before(target: ActionId, before: ActionId): Either<ChangeOrderActionApplicationServiceException, Unit>

    fun after(target: ActionId, after: ActionId): Either<ChangeOrderActionApplicationServiceException, Unit>
}
