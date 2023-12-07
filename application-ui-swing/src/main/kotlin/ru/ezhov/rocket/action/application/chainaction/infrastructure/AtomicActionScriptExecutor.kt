package ru.ezhov.rocket.action.application.chainaction.infrastructure

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.io.File

class AtomicActionScriptExecutor(
    private val engineFactory: EngineFactory,
    private val variablesApplication: VariablesApplication,
    private val atomicActionService: AtomicActionService,
) {
    fun executeScript(inputValue: Any?, atomicAction: AtomicAction): Any? {
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
                    ) +
                    EngineVariable(
                        name = ActionExecutor.ATOMIC_ACTION_EXECUTOR_ARG,
                        value = AtomicActionExecutorPublicApiImpl(
                            engineFactory = engineFactory,
                            variablesApplication = variablesApplication,
                            atomicActionService = atomicActionService,
                        ),
                    )
            )
    }
}
