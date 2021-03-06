package ru.ezhov.rocket.action.core.application.changeorder

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.core.domain.changeorder.AfterChangeOrderService
import ru.ezhov.rocket.action.core.domain.changeorder.BeforeChangeOrderService
import ru.ezhov.rocket.action.core.domain.model.ActionId

class ChangeOrderActionApplicationServiceImpl(
    private val afterService: AfterChangeOrderService,
    private val beforeService: BeforeChangeOrderService,
) : ChangeOrderActionApplicationService {
    override fun before(target: ActionId, before: ActionId): Either<ChangeOrderActionApplicationServiceException, Unit> =
        beforeService.before(target, before)
            .handleErrorWith { e ->
                Either.Left(ChangeOrderActionApplicationServiceException(
                    message = "Error when set action order with id=${target.value} before action with id=${before.value}",
                    cause = e
                ))
            }

    override fun after(target: ActionId, after: ActionId): Either<ChangeOrderActionApplicationServiceException, Unit> =
        afterService.after(target, after)
            .handleErrorWith { e ->
                Either.Left(ChangeOrderActionApplicationServiceException(
                    message = "Error when set action order with id=${target.value} before action with id=${after.value}",
                    cause = e
                ))
            }
}
