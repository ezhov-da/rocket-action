package ru.ezhov.rocket.action.core.application.change

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.core.domain.model.ActionSettings
import ru.ezhov.rocket.action.core.domain.repository.ActionSettingsRepository

class UpdateActionSettingsApplicationServiceImpl(
    private val actionSettingsRepository: ActionSettingsRepository
) : UpdateActionSettingsApplicationService {
    override fun `do`(settings: ActionSettings): Either<UpdateActionSettingsApplicationServiceException, Unit> =
        actionSettingsRepository.save(settings)
            .handleErrorWith { e ->
                Either.Left(UpdateActionSettingsApplicationServiceException(
                    message = "Error when change action settings",
                    cause = e
                ))
            }
}
