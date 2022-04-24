package ru.ezhov.rocket.action.core.domain.changeorder

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.ActionId

interface AfterChangeOrderService {
    fun after(target: ActionId, after: ActionId): Either<ChangeOrderServiceException, Unit>
}
