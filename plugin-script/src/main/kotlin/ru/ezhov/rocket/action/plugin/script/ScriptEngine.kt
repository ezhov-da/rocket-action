package ru.ezhov.rocket.action.plugin.script

import arrow.core.Either

interface ScriptEngine {
    companion object {
        const val VARIABLES_NAME = "_variables"
    }

    fun type(): ScriptEngineType

    fun execute(script: String, variables: Map<String, String>): Either<ScriptEngineException, Any?>
}
