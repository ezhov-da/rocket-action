package ru.ezhov.rocket.action.core.application.change

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionSettings

interface ChangeActionApplicationService {
    fun `do`(action: Action): Either<ChangeActionApplicationServiceException, Unit>

    fun `do`(actionSettings: ActionSettings): Either<ChangeActionSettingsApplicationServiceException, Unit>
}
