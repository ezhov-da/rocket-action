package ru.ezhov.rocket.action.core.application.create

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.core.domain.changeorder.AfterChangeOrderService
import ru.ezhov.rocket.action.core.domain.changeorder.BeforeChangeOrderService
import ru.ezhov.rocket.action.core.domain.model.NewAction
import ru.ezhov.rocket.action.core.domain.repository.ActionRepository
import ru.ezhov.rocket.action.core.domain.repository.ActionSettingsRepository

class CreateActionApplicationServiceImpl(
    private val actionRepository: ActionRepository,
    private val actionSettingsRepository: ActionSettingsRepository,
    private val afterChangeOrderService: AfterChangeOrderService,
    private val beforeChangeOrderService: BeforeChangeOrderService,

    ) : CreateActionApplicationService {
    override fun `do`(new: NewAction): Either<CreateActionApplicationServiceException, Unit> {
        val action = new.toAction()
        val actionSettings = new.toActionSettings()
        return actionRepository
            .addOrUpdate(listOf(action))
            .flatMap {
                actionSettingsRepository.save(actionSettings)
            }.flatMap {
                if (new.order.isAfter()) {
                    afterChangeOrderService.after(action.id, new.order.afterId!!)
                } else {
                    beforeChangeOrderService.before(action.id, new.order.beforeId!!)
                }
            }
            .handleErrorWith { ex ->
                Either.Left(CreateActionApplicationServiceException(
                    message = "Error when add new action",
                    cause = ex
                ))
            }
    }
}
