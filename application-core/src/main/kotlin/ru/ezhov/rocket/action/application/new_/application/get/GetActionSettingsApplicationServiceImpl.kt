package ru.ezhov.rocket.action.application.new_.application.get

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings
import ru.ezhov.rocket.action.application.new_.domain.repository.ActionSettingsRepository

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
}
