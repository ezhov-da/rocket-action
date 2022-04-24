package ru.ezhov.rocket.action.application.new_.application.create

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.NewAction

interface CreateActionApplicationService {
    fun `do`(new: NewAction): Either<CreateActionApplicationServiceException, Unit>
}
