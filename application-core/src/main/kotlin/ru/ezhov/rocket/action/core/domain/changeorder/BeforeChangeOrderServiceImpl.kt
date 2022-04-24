package ru.ezhov.rocket.action.core.domain.changeorder

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleErrorWith
import arrow.core.zip
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.repository.ActionRepository

class BeforeChangeOrderServiceImpl(
    private val actionRepository: ActionRepository
) : ru.ezhov.rocket.action.core.domain.changeorder.BeforeChangeOrderService {
    override fun before(target: ActionId, before: ActionId): Either<ChangeOrderServiceException, Unit> =
        action(target)
            .zip(fb = action(before))
            .handleErrorWith { e ->
                Either.Left(ChangeOrderServiceException("Error when change order before", e))
            }
            .flatMap { (actionSet, actionBefore) ->
                privateBefore(actionSet, actionBefore)
            }

    private fun action(id: ActionId) = actionRepository.action(id)

    private fun privateBefore(target: Action?, before: Action?): Either<ChangeOrderServiceException, Unit> =
        exceptionIfActionNotExists(target)
            .zip(exceptionIfActionNotExists(target))
            .flatMap { (setAction, beforeAction) ->
                when (setAction.parentId == beforeAction.parentId) {
                    true -> changeOrderIfEqualsParent(target = setAction, before = setAction)
                    false -> changeOrderIfNotEqualsParent(target = setAction, before = setAction)
                }
            }

    private fun changeOrderIfEqualsParent(target: Action, before: Action): Either<ChangeOrderServiceException, Unit> {
        val setWithNewOrder = target.withNewOrder(before.order)
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

    private fun changeOrderIfNotEqualsParent(target: Action, before: Action): Either<ChangeOrderServiceException, Unit> {
        val changedTargetAction = target
            .withNewOrder(before.order)
            .withNewParent(before.parentId)
        return setOrderActionsAfterActionAndSave(changedTargetAction)
    }

    private fun exceptionIfActionNotExists(action: Action?): Either<ChangeOrderServiceException, Action> =
        when (action) {
            null -> Either.Left(ChangeOrderServiceException("Action not found"))
            else -> Either.Right(action)
        }
}
