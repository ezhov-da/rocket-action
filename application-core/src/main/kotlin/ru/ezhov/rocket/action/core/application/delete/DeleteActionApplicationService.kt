package ru.ezhov.rocket.action.core.application.delete

import arrow.core.Either
import ru.ezhov.rocket.action.core.domain.model.ActionId

interface DeleteActionApplicationService {
    fun `do`(id: ActionId, withAllChildrenRecursive: Boolean): Either<DeleteActionApplicationServiceException, Unit>
}
