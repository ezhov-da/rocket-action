package ru.ezhov.rocket.action.application.new_.application.delete

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.repository.ActionAndSettingsRepository

class DeleteActionApplicationServiceImpl(
    private val actionAndSettingsRepository: ActionAndSettingsRepository
) : DeleteActionApplicationService {
    override fun `do`(
        id: ActionId, withAllChildrenRecursive: Boolean
    ): Either<DeleteActionApplicationServiceException, Unit> =
        actionAndSettingsRepository.remove(id, withAllChildrenRecursive)
            .handleErrorWith { e ->
                Either.Left(DeleteActionApplicationServiceException(
                    message = "Error when delete action by id=${id.value} " +
                        "withAllChildrenRecursive=$withAllChildrenRecursive",
                    cause = e,
                ))
            }
}
