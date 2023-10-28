package ru.ezhov.rocket.action.application.chainaction.infrastructure

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutorProgress
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.io.File

private const val INPUT_NAME_ARG = "_INPUT"

@Service
class ChainActionExecutorImpl(
    private val engineFactory: EngineFactory,
    private val variablesApplication: VariablesApplication,
) : ChainActionExecutor {
    override fun execute(
        input: Any?,
        chainAction: ChainAction,
        chainActionExecutorProgress: ChainActionExecutorProgress
    ) {
        var currentIndex = 0
        var currentAtomicAction: AtomicAction? = null
        var lastResult: Any? = null
        try {
            var inputValue = input
            chainAction.actions.forEachIndexed { index, atomicAction ->
                currentIndex = index + 1
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
                                name = INPUT_NAME_ARG,
                                value = inputValue,
                            )
                    )

                inputValue = executeResult
                lastResult = executeResult
                chainActionExecutorProgress.success(currentIndex, currentAtomicAction!!)
            }

            chainActionExecutorProgress.complete(lastResult)
        } catch (ex: Exception) {
            chainActionExecutorProgress.failure(currentIndex, currentAtomicAction!!, ex)
        }
    }
}
