package ru.ezhov.rocket.action.plugin.script.kotlin.application

import arrow.core.Either

interface ScriptEngine {
    fun execute(script: String): Either<ScriptEngineException, Any?>
}
