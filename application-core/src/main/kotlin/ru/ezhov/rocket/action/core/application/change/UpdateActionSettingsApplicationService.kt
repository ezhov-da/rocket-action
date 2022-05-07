package ru.ezhov.rocket.action.core.application.change

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.ActionSettings

interface UpdateActionSettingsApplicationService {
    fun `do`(settings: ActionSettings): Either<UpdateActionSettingsApplicationServiceException, Unit>
}
