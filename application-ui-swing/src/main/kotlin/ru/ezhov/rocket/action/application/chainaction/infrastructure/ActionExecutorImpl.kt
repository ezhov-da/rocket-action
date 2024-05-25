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
        when (action) {
            is ChainAction -> executeChainAction(input, action, progressExecutingAction)
            is AtomicAction -> executeAtomicAction(input, action, progressExecutingAction)
        }
    }

    private fun executeChainAction(
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
                        currentAtomicActionOrderId!!,
                        null,
                        IllegalStateException(
                            "Action with ID '${currentAtomicActionId!!}' not found for chain '${chainAction.name}'"
                        )
                    )
                    return
                }

                currentAtomicAction = atomicAction

                val executeResult = executeScript(inputValue, atomicAction)

                inputValue = executeResult
                lastResult = executeResult
                progressExecutingAction.onAtomicActionSuccess(
                    currentAtomicActionOrderId!!,
                    lastResult,
                    currentAtomicAction!!
                )
            }

            progressExecutingAction.onComplete(lastResult, currentAtomicAction!!)

            logger.debug { "Chain action by ID '${chainAction.id}'. Input '$input'. Completed" }
        } catch (ex: Exception) {
            progressExecutingAction.onAtomicActionFailure(currentAtomicActionOrderId!!, currentAtomicAction!!, ex)
        }
    }

    private fun executeScript(
        inputValue: Any?, atomicAction: AtomicAction
    ) = AtomicActionScriptExecutor(
        engineFactory = engineFactory,
        variablesApplication = variablesApplication,
        atomicActionService = atomicActionService,
    )
        .executeScript(inputValue, atomicAction)


    private fun executeAtomicAction(
        input: Any?,
        atomicAction: AtomicAction,
        progressExecutingAction: ProgressExecutingAction
    ) {
        try {
            logger.debug { "Run atomic action by ID '${atomicAction.id}'. Input '$input'" }

            val executeResult = executeScript(input, atomicAction)

            progressExecutingAction.onAtomicActionSuccess(
                atomicAction.id,
                executeResult,
                atomicAction
            )

            progressExecutingAction.onComplete(executeResult, atomicAction)

            logger.debug { "Atomic action by ID '${atomicAction.id}'. Input '$input'. Completed" }
        } catch (ex: Exception) {
            progressExecutingAction.onAtomicActionFailure(atomicAction.id, atomicAction, ex)
        }
    }
}
