package ru.ezhov.rocket.action.application.new_.application.delete

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface DeleteActionApplicationService {
    fun `do`(id: ActionId, withAllChildrenRecursive: Boolean): Either<DeleteActionApplicationServiceException, Unit>
}
