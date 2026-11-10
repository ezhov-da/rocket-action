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
        var currentAtomicAction: AtomicAction? = null
        var lastResult: Any? = null
        try {
            logger.debug { "Run chain action by ID '${chainAction.id}'. Input '$input'" }

            var inputValue = input
            chainAction.actions.forEach { atomicActionOrder ->
                val orderId = atomicActionOrder.chainOrderId
                currentAtomicActionOrderId = orderId
                val atomicActionId = atomicActionOrder.actionId
                val atomicAction = atomicActionService.atomicBy(atomicActionId)
                if (atomicAction == null) {
                    progressExecutingAction.onAtomicActionFailure(
                        orderId = orderId,
                        atomicAction = null,
                        ex = IllegalStateException(
                            "Action with ID '$atomicActionId' not found for chain '${chainAction.name}'"
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
                    orderId = orderId,
                    result = lastResult,
                    atomicAction = atomicAction
                )
            }

            val lastAtomicAction = currentAtomicAction
            if (lastAtomicAction == null) {
                progressExecutingAction.onAtomicActionFailure(
                    orderId = chainAction.id,
                    atomicAction = null,
                    ex = IllegalStateException(
                        "Chain action '${chainAction.name}' with ID '${chainAction.id}' has no actions"
                    )
                )
                return
            }

            progressExecutingAction.onComplete(result = lastResult, lastAtomicAction = lastAtomicAction)

            logger.debug { "Chain action by ID '${chainAction.id}'. Input '$input'. Completed" }
        } catch (ex: Exception) {
            // The failure callback must always be called, even when the error happened
            // before the current atomic action was resolved.
            progressExecutingAction.onAtomicActionFailure(
                orderId = currentAtomicActionOrderId ?: chainAction.id,
                atomicAction = currentAtomicAction,
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
