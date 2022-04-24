package ru.ezhov.rocket.action.core.domain.changeorder

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleErrorWith
import arrow.core.zip
import ru.ezhov.rocket.action.core.domain.repository.ActionRepository
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId

class AfterChangeOrderServiceImpl(
    private val actionRepository: ActionRepository
) : AfterChangeOrderService {
    override fun after(target: ActionId, after: ActionId): Either<ChangeOrderServiceException, Unit> =
        action(target)
            .zip(fb = action(after))
            .handleErrorWith { e ->
                Either.Left(ChangeOrderServiceException("Error when change order after", e))
            }
            .flatMap { (actionSet, actionBefore) ->
                privateAfter(actionSet, actionBefore)
            }

    private fun action(id: ActionId) = actionRepository.action(id)

    private fun privateAfter(target: Action?, after: Action?): Either<ChangeOrderServiceException, Unit> =
        exceptionIfActionNotExists(target)
            .zip(exceptionIfActionNotExists(after))
            .flatMap { (setAction, afterAction) ->
                when (setAction.parentId == afterAction.parentId) {
                    true -> changeOrderIfEqualsParent(target = setAction, after = setAction)
                    false -> changeOrderIfNotEqualsParent(target = setAction, after = setAction)
                }
            }

    private fun changeOrderIfEqualsParent(target: Action, after: Action): Either<ChangeOrderServiceException, Unit> {
        val setWithNewOrder = target.withNewOrder(after.order.plusOne())
        return setOrderActionsAfterActionAndSave(setWithNewOrder)
    }

    private fun setOrderActionsAfterActionAndSave(action: Action): Either<ChangeOrderServiceException, Unit> =
        actionRepository.children(action.parentId)
            .flatMap { actionsWithCommonParent ->
                val actionsWithChangedOrders = actionsWithCommonParent
                    .filter { it.order.value >= action.order.value }
                    .map { it.withNewOrder(it.order.plusOne()) }
                    .toMutableList()
                actionsWithChangedOrders.add(action)

                actionRepository.addOrUpdate(actionsWithChangedOrders)

                Either.Right(Unit)
            }
            .handleErrorWith { e ->
                Either.Left(
                    ChangeOrderServiceException(
                        message = "Error change order",
                        cause = e
                    )
                )
            }

    private fun changeOrderIfNotEqualsParent(target: Action, after: Action): Either<ChangeOrderServiceException, Unit> {
        val changedTargetAction = target
            .withNewOrder(after.order.plusOne())
            .withNewParent(after.parentId)
        return setOrderActionsAfterActionAndSave(changedTargetAction)
    }

    private fun exceptionIfActionNotExists(action: Action?): Either<ChangeOrderServiceException, Action> =
        when (action) {
            null -> Either.Left(ChangeOrderServiceException("Action not found"))
            else -> Either.Right(action)
        }
}
