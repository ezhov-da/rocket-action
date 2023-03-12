package ru.ezhov.rocket.action.plugin.script

import ru.ezhov.rocket.action.plugin.script.groovy.application.GroovyScriptEngine
import ru.ezhov.rocket.action.plugin.script.kotlin.application.KotlinScriptEngine

object ScriptEngineFactory {
    private val groovy = GroovyScriptEngine()
    private val kotlin = KotlinScriptEngine()

    fun engine(type: ScriptEngineType): ScriptEngine = when (type) {
        ScriptEngineType.KOTLIN -> kotlin
        ScriptEngineType.GROOVY -> groovy
    }
}
