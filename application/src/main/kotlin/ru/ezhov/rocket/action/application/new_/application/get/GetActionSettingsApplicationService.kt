package ru.ezhov.rocket.action.application.new_.application.get

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings

interface GetActionSettingsApplicationService {
    fun settings(id: ActionId): Either<GetActionSettingsApplicationServiceException, ActionSettings?>
}
