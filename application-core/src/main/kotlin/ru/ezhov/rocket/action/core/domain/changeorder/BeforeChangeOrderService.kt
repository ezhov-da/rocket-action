package ru.ezhov.rocket.action.core.domain.changeorder

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.ActionId

interface BeforeChangeOrderService {
    fun before(target: ActionId, before: ActionId): Either<ChangeOrderServiceException, Unit>
}
