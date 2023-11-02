package ru.ezhov.rocket.action.application.chainaction.application

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction

@Service
class ActionExecutorService(
    private val actionExecutor: ActionExecutor,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) {
    fun execute(input: String?, actionId: String, progressExecutingAction: ProgressExecutingAction) {
        val action = chainActionService.byId(actionId) ?: atomicActionService.atomicBy(actionId)!!
        actionExecutor.execute(input, action, progressExecutingAction)
    }
}
