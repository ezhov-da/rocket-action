package ru.ezhov.rocket.action.core.application.get

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.ActionSettings
import ru.ezhov.rocket.action.core.domain.repository.ActionSettingsRepository

class GetActionSettingsApplicationServiceImpl(
    private val actionSettingsRepository: ActionSettingsRepository
) : GetActionSettingsApplicationService {
    override fun settings(id: ActionId): Either<GetActionSettingsApplicationServiceException, ActionSettings?> =
        actionSettingsRepository.settings(id)
            .handleErrorWith { e ->
                Either.Left(GetActionSettingsApplicationServiceException(
                    message = "Error when get action settings by id=${id.value}",
                    cause = e
                ))
            }

    override fun all(): Either<GetActionSettingsApplicationServiceException, List<ActionSettings>> =
        actionSettingsRepository.all()
            .handleErrorWith { e ->
                Either.Left(GetActionSettingsApplicationServiceException(
                    message = "Error when get all action settings",
                    cause = e
                ))
            }
}
