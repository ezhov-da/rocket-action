package ru.ezhov.rocket.action.plugin.script.kotlin.application

import arrow.core.Either
import mu.KotlinLogging
import ru.ezhov.rocket.action.plugin.script.ScriptEngine
import ru.ezhov.rocket.action.plugin.script.ScriptEngine.Companion.VARIABLES_NAME
import ru.ezhov.rocket.action.plugin.script.ScriptEngineException
import ru.ezhov.rocket.action.plugin.script.ScriptEngineType
import javax.script.ScriptEngineManager

private val logger = KotlinLogging.logger {}

class KotlinScriptEngine(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
) : ScriptEngine {
    override fun type(): ScriptEngineType = ScriptEngineType.KOTLIN

    override fun execute(script: String, variables: Map<String, String>): Either<ScriptEngineException, Any?> =
        try {
            logger.debug { "Usage class loader for script engine '${classLoader::class.qualifiedName}'" }

            val scriptEngineManager = ScriptEngineManager(classLoader)
            val scriptEngine = scriptEngineManager.getEngineByExtension("kts")
            if (scriptEngine == null) {
                Either.Left(ScriptEngineException(message = "Engine for 'kts' not found"))
            } else {
                val bindings = scriptEngine.createBindings()
                variables.forEach { bindings[it.key] = it.value }
                bindings[VARIABLES_NAME] = HashMap<String, String>(variables)
                Either.Right(scriptEngine.eval(script, bindings))
            }
        } catch (e: Exception) {
            Either.Left(ScriptEngineException(message = "Error when execute script '$script'", cause = e))
        }
}
