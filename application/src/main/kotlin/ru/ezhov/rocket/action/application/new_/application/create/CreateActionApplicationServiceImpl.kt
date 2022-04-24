package ru.ezhov.rocket.action.application.new_.application.create

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.application.new_.domain.model.NewAction
import ru.ezhov.rocket.action.application.new_.domain.repository.ActionAndSettingsRepository

class CreateActionApplicationServiceImpl(
    private val actionAndSettingsRepository: ActionAndSettingsRepository
) : CreateActionApplicationService {
    override fun `do`(new: NewAction): Either<CreateActionApplicationServiceException, Unit> =
        actionAndSettingsRepository.add(new)
            .handleErrorWith { e ->
                Either.Left(CreateActionApplicationServiceException(
                    message = "Error when add new action",
                    cause = e
                ))
            }
}
