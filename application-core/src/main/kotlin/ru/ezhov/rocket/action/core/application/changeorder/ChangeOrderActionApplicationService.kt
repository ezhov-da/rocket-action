package ru.ezhov.rocket.action.core.application.changeorder

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.ActionId

interface ChangeOrderActionApplicationService {
    fun before(target: ActionId, before: ActionId): Either<ChangeOrderActionApplicationServiceException, Unit>

    fun after(target: ActionId, after: ActionId): Either<ChangeOrderActionApplicationServiceException, Unit>
}
