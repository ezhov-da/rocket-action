package ru.ezhov.rocket.action.core.application.change

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionSettings
import ru.ezhov.rocket.action.core.domain.repository.ActionRepository
import ru.ezhov.rocket.action.core.domain.repository.ActionSettingsRepository

class ChangeActionApplicationServiceImpl(
    private val actionRepository: ActionRepository,
    private val actionSettingsRepository: ActionSettingsRepository,
) : ChangeActionApplicationService {
    override fun `do`(action: Action): Either<ChangeActionApplicationServiceException, Unit> =
        actionRepository.addOrUpdate(listOf(action))
            .handleErrorWith { e ->
                Either.Left(ChangeActionApplicationServiceException(
                    message = "Error when change action",
                    cause = e
                ))
            }

    override fun `do`(actionSettings: ActionSettings): Either<ChangeActionSettingsApplicationServiceException, Unit> =
        actionSettingsRepository.save(actionSettings)
            .handleErrorWith { e ->
                Either.Left(ChangeActionSettingsApplicationServiceException(
                    message = "Error when change action",
                    cause = e
                ))
            }
}
