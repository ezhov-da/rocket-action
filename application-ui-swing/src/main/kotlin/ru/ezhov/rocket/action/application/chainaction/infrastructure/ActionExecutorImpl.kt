package ru.ezhov.rocket.action.application.chainaction.infrastructure

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger { }

@Service
class ActionExecutorImpl(
    private val engineFactory: EngineFactory,
    private val variablesApplication: VariablesApplication,
    private val atomicActionService: AtomicActionService,
) : ActionExecutor {
    override fun execute(
        input: Any?,
        action: Action,
        progressExecutingAction: ProgressExecutingAction
    ) {
        val context = ConcurrentHashMap<String, Any?>()
        when (action) {
            is ChainAction -> executeChainAction(
                context = context,
                input = input,
                chainAction = action,
                progressExecutingAction = progressExecutingAction
            )

            is AtomicAction -> executeAtomicAction(context, input, action, progressExecutingAction)
        }
    }

    private fun executeChainAction(
        context: Map<String, Any?>,
        input: Any?,
        chainAction: ChainAction,
        progressExecutingAction: ProgressExecutingAction
    ) {
        var currentAtomicActionOrderId: String? = null
        var currentAtomicActionId: String?
        var currentAtomicAction: AtomicAction? = null
        var lastResult: Any? = null
        try {
            logger.debug { "Run chain action by ID '${chainAction.id}'. Input '$input'" }

            var inputValue = input
            chainAction.actions.forEach { atomicActionOrder ->
                currentAtomicActionOrderId = atomicActionOrder.chainOrderId
                currentAtomicActionId = atomicActionOrder.actionId
                val atomicAction = atomicActionService.atomicBy(currentAtomicActionId!!)
                if (atomicAction == null) {
                    progressExecutingAction.onAtomicActionFailure(
                        orderId = currentAtomicActionOrderId!!,
                        atomicAction = null,
                        ex = IllegalStateException(
                            "Action with ID '${currentAtomicActionId!!}' not found for chain '${chainAction.name}'"
                        )
                    )
                    return
                }

                currentAtomicAction = atomicAction

                val executeResult = executeScript(
                    context = context,
                    inputValue = inputValue,
                    atomicAction = atomicAction
                )

                inputValue = executeResult
                lastResult = executeResult
                progressExecutingAction.onAtomicActionSuccess(
                    orderId = currentAtomicActionOrderId!!,
                    result = lastResult,
                    atomicAction = currentAtomicAction!!
                )
            }

            progressExecutingAction.onComplete(result = lastResult, lastAtomicAction = currentAtomicAction!!)

            logger.debug { "Chain action by ID '${chainAction.id}'. Input '$input'. Completed" }
        } catch (ex: Exception) {
            progressExecutingAction.onAtomicActionFailure(
                orderId = currentAtomicActionOrderId!!,
                atomicAction = currentAtomicAction!!,
                ex = ex
            )
        }
    }

    private fun executeScript(
        context: Map<String, Any?>,
        inputValue: Any?,
        atomicAction: AtomicAction
    ) = AtomicActionScriptExecutor(
        engineFactory = engineFactory,
        variablesApplication = variablesApplication,
        atomicActionService = atomicActionService,
        contextMap = context
    )
        .executeScript(inputValue, atomicAction)


    private fun executeAtomicAction(
        context: Map<String, Any?>,
        input: Any?,
        atomicAction: AtomicAction,
        progressExecutingAction: ProgressExecutingAction
    ) {
        try {
            logger.debug { "Run atomic action by ID '${atomicAction.id}'. Input '$input'" }

            val executeResult = executeScript(context, input, atomicAction)

            progressExecutingAction.onAtomicActionSuccess(
                orderId = atomicAction.id,
                result = executeResult,
                atomicAction = atomicAction
            )

            progressExecutingAction.onComplete(result = executeResult, lastAtomicAction = atomicAction)

            logger.debug { "Atomic action by ID '${atomicAction.id}'. Input '$input'. Completed" }
        } catch (ex: Exception) {
            progressExecutingAction.onAtomicActionFailure(
                orderId = atomicAction.id,
                atomicAction = atomicAction,
                ex = ex
            )
        }
    }
}
