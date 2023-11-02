package ru.ezhov.rocket.action.application.chainaction.infrastructure

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.io.File


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
        var currentAtomicActionId: String? = null
        var currentAtomicAction: AtomicAction? = null
        var lastResult: Any? = null
        try {
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
        } catch (ex: Exception) {
            progressExecutingAction.onAtomicActionFailure(currentAtomicActionOrderId!!, currentAtomicAction!!, ex)
        }
    }

    private fun executeScript(inputValue: Any?, atomicAction: AtomicAction): Any? {
        val script = when (atomicAction.source) {
            AtomicActionSource.FILE -> File(atomicAction.data).readText()
            AtomicActionSource.TEXT -> atomicAction.data
        }

        val engine = when (atomicAction.engine) {
            AtomicActionEngine.KOTLIN -> engineFactory.by(EngineType.KOTLIN)
            AtomicActionEngine.GROOVY -> engineFactory.by(EngineType.GROOVY)
        }

        return engine
            .execute(
                template = script,
                variables = variablesApplication
                    .all()
                    .variables
                    .map {
                        EngineVariable(
                            name = it.name,
                            value = it.value,
                        )
                    } +
                    EngineVariable(
                        name = ActionExecutor.INPUT_NAME_ARG,
                        value = inputValue,
                    )
            )
    }

    private fun executeAtomicAction(
        input: Any?,
        atomicAction: AtomicAction,
        progressExecutingAction: ProgressExecutingAction
    ) {
        try {
            val executeResult = executeScript(input, atomicAction)

            progressExecutingAction.onAtomicActionSuccess(
                atomicAction.id,
                executeResult,
                atomicAction
            )

            progressExecutingAction.onComplete(executeResult, atomicAction)
        } catch (ex: Exception) {
            progressExecutingAction.onAtomicActionFailure(atomicAction.id, atomicAction, ex)
        }
    }
}
