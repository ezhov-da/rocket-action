package ru.ezhov.rocket.action.plugin.script.groovy.application

import arrow.core.Either
import groovy.lang.Binding
import groovy.lang.GroovyShell
import ru.ezhov.rocket.action.plugin.script.ScriptEngine
import ru.ezhov.rocket.action.plugin.script.ScriptEngine.Companion.VARIABLES_NAME
import ru.ezhov.rocket.action.plugin.script.ScriptEngineException
import ru.ezhov.rocket.action.plugin.script.ScriptEngineType

class GroovyScriptEngine : ScriptEngine {
    override fun type(): ScriptEngineType = ScriptEngineType.GROOVY

    override fun execute(script: String, variables: Map<String, String>): Either<ScriptEngineException, Any?> =
        try {
            val sharedData = Binding()
            val groovyShell = GroovyShell(sharedData)
            variables.forEach { sharedData.setProperty(it.key, it.value) }
            sharedData.setProperty(VARIABLES_NAME, variables)
            Either.Right(groovyShell.evaluate(script).toString())
        } catch (e: Exception) {
            Either.Left(
                ScriptEngineException(
                    message = "Error when execute script '$script'",
                    cause = e
                )
            )
        }
}
