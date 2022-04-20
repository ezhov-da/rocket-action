package ru.ezhov.rocket.action.plugin.script.kotlin.application

import arrow.core.Either
import javax.script.ScriptEngineManager

class KotlinScriptEngine : ScriptEngine {
    override fun execute(script: String): Either<ScriptEngineException, Any> =
        try {
            val scriptEngine = ScriptEngineManager().getEngineByExtension("kts")
            Either.Right(scriptEngine.eval(script))
        } catch (e: Exception) {
            Either.Left(ScriptEngineException(message = "Error when execute script '$script'", cause = e))
        }
}
