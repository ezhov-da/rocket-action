package ru.ezhov.rocket.action.core.application.get

import arrow.core.Either
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.repository.ActionRepository

class GetActionApplicationServiceImpl(
    private val actionRepository: ActionRepository
) : GetActionApplicationService {
    override fun children(id: ActionId): Either<GetActionApplicationServiceException, List<Action>> =
        actionRepository.children(id)
            .handleErrorWith { e ->
                Either.Left(GetActionApplicationServiceException(
                    message = "Error when get children for action id=${id.value}",
                    cause = e
                ))
            }

    override fun action(id: ActionId): Either<GetActionApplicationServiceException, Action?> =
        actionRepository.action(id)
            .handleErrorWith { e ->
                Either.Left(GetActionApplicationServiceException(
                    message = "Error when get action by id=${id.value}",
                    cause = e
                ))
            }

    override fun all(): Either<GetActionApplicationServiceException, List<Action>> =
        actionRepository.all()
            .handleErrorWith { e ->
                Either.Left(GetActionApplicationServiceException(
                    message = "Error when get all actions",
                    cause = e
                ))
            }
}
