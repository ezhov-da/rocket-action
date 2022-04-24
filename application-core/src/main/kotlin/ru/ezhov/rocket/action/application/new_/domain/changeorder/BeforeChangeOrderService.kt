package ru.ezhov.rocket.action.application.new_.domain.changeorder

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface BeforeChangeOrderService {
    fun before(target: ActionId, before: ActionId): Either<ChangeOrderServiceException, Unit>
}
