package ru.ezhov.rocket.action.core.application.create

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.core.domain.model.NewAction
import ru.ezhov.rocket.action.core.domain.repository.ActionAndSettingsRepository

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
