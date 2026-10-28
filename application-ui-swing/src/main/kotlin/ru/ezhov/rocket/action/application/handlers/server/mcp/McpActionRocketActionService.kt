package ru.ezhov.rocket.action.application.handlers.server.mcp

import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction


private const val RESULT_KEY = "result"

@Component
class McpActionRocketActionService(
    private val atomicActionService: AtomicActionService,
    private val actionExecutor: ActionExecutor
) {
    fun all(): List<AtomicAction> = atomicActionService.atomics()

    fun execute(id: String, text: String?): RocketActionHandleStatus {
        val action = atomicActionService.atomicBy(id)
            ?: return RocketActionHandleStatus.InvalidInputData(listOf("Action by ID '$id' is not found"))

        var executeResult: RocketActionHandleStatus? = null

        actionExecutor.execute(
            input = text,
            action = action,
            progressExecutingAction = object : ProgressExecutingAction {
                override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                    executeResult = if (result == null) {
                        RocketActionHandleStatus.Success()
                    } else {
                        RocketActionHandleStatus.Success(mapOf(RESULT_KEY to result.toString()))

                    }
                }

                override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                    // not interesting
                }

                override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                    executeResult = RocketActionHandleStatus.Error(
                        "Error when execute atomic action with ID '${atomicAction?.id}'", ex
                    )
                }
            }
        )

        return executeResult!!
    }
}
