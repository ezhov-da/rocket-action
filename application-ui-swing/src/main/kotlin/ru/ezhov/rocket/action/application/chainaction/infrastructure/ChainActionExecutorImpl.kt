package ru.ezhov.rocket.action.application.chainaction.infrastructure

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutorProgress
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
class ChainActionExecutorImpl(
    private val engineFactory: EngineFactory,
    private val variablesApplication: VariablesApplication,
    private val atomicActionService: AtomicActionService,
) : ChainActionExecutor {
    override fun execute(
        input: Any?,
        chainAction: ChainAction,
        chainActionExecutorProgress: ChainActionExecutorProgress
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
                    chainActionExecutorProgress.onAtomicActionFailure(
                        currentAtomicActionOrderId!!,
                        null,
                        IllegalStateException(
                            "Action with ID '${currentAtomicActionId!!}' not found for chain '${chainAction.name}'"
                        )
                    )
                    return
                }

                currentAtomicAction = atomicAction

                val script = when (atomicAction.source) {
                    AtomicActionSource.FILE -> File(atomicAction.data).readText()
                    AtomicActionSource.TEXT -> atomicAction.data
                }

                val engine = when (atomicAction.engine) {
                    AtomicActionEngine.KOTLIN -> engineFactory.by(EngineType.KOTLIN)
                    AtomicActionEngine.GROOVY -> engineFactory.by(EngineType.GROOVY)
                }

                val executeResult = engine
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
                                name = ChainActionExecutor.INPUT_NAME_ARG,
                                value = inputValue,
                            )
                    )

                inputValue = executeResult
                lastResult = executeResult
                chainActionExecutorProgress.onAtomicActionSuccess(
                    currentAtomicActionOrderId!!,
                    lastResult,
                    currentAtomicAction!!
                )
            }

            chainActionExecutorProgress.onChainComplete(lastResult, currentAtomicAction!!)
        } catch (ex: Exception) {
            chainActionExecutorProgress.onAtomicActionFailure(currentAtomicActionOrderId!!, currentAtomicAction!!, ex)
        }
    }
}
