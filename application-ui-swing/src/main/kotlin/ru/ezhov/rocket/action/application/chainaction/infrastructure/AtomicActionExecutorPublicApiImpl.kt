package ru.ezhov.rocket.action.application.chainaction.infrastructure

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.AtomicActionExecutorPublicApi
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication

class AtomicActionExecutorPublicApiImpl(
    private val engineFactory: EngineFactory,
    private val variablesApplication: VariablesApplication,
    private val atomicActionService: AtomicActionService,
    private val contextMap: Map<String, Any?>
) : AtomicActionExecutorPublicApi {
    override fun execute(alias: String, arg: Any?): Any? {
        val atomic = atomicActionService.byAlias(alias)
            ?: throw IllegalStateException("Atomic action with alias '$alias' is not found")

        return AtomicActionScriptExecutor(
            engineFactory = engineFactory,
            variablesApplication = variablesApplication,
            atomicActionService = atomicActionService,
            contextMap = contextMap,
        ).executeScript(arg, atomic)
    }
}
