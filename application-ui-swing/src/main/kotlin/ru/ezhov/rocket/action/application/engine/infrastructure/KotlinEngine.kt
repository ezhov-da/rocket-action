package ru.ezhov.rocket.action.application.engine.infrastructure

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.engine.domain.Engine
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import javax.script.ScriptEngineManager
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

class KotlinEngine : Engine {

    companion object {
        private const val VARIABLES_NAME = "_variables"
    }

    override fun execute(template: String, variables: List<EngineVariable>): Any? =
        try {
            var result: Any?
            val time = measureTimeMillis {
                val scriptEngineManager = ScriptEngineManager()
                val scriptEngine = scriptEngineManager.getEngineByExtension("kts")
                if (scriptEngine == null) {
                    throw NullPointerException("Engine for 'kts' not found")
                } else {
                    val bindings = scriptEngine.createBindings()
                    variables.forEach { bindings[it.name] = it.value }
                    bindings[VARIABLES_NAME] = variables.associate { it.name to it.value }
                    result = scriptEngine.eval(template, bindings)
                }
            }

            logger.debug {
                "Time execute kotlin script '$time'ms. " +
                    "Template='$template', variables=${variables.associate { it.name to it.value }}"
            }

            result
        } catch (e: Exception) {
            throw Exception("Error when execute script '$template'", e)
        }
}
