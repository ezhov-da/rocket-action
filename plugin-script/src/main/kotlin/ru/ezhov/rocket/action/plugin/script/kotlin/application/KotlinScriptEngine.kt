package ru.ezhov.rocket.action.plugin.script.kotlin.application

import arrow.core.Either
import mu.KotlinLogging
import javax.script.ScriptEngineManager
import kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory

private val logger = KotlinLogging.logger {}

class KotlinScriptEngine(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
) : ScriptEngine {
    override fun execute(script: String): Either<ScriptEngineException, Any?> =
        try {
            logger.debug { "Usage class loader for script engine '${classLoader::class.qualifiedName}'" }

            val scriptEngineManager = ScriptEngineManager(classLoader)
            val scriptEngine = scriptEngineManager.getEngineByExtension("kts")
            if (scriptEngine == null) {
                Either.Left(ScriptEngineException(message = "Engine for 'kts' not found"))
            } else {
                Either.Right(scriptEngine.eval(script))
            }
        } catch (e: Exception) {
            Either.Left(ScriptEngineException(message = "Error when execute script '$script'", cause = e))
        }
}
