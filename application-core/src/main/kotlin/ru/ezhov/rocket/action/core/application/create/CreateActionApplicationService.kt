package ru.ezhov.rocket.action.core.application.create

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.NewAction

interface CreateActionApplicationService {
    fun `do`(new: NewAction): Either<CreateActionApplicationServiceException, Unit>
}
