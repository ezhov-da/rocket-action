package ru.ezhov.rocket.action.core.application.get

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.ActionSettings

interface GetActionSettingsApplicationService {
    fun settings(id: ActionId): Either<GetActionSettingsApplicationServiceException, ActionSettings?>

    fun all(): Either<GetActionSettingsApplicationServiceException, List<ActionSettings>>
}
