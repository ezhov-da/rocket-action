package ru.ezhov.rocket.action.plugin.script

import ru.ezhov.rocket.action.plugin.script.groovy.application.GroovyScriptEngine

object ScriptEngineFactory {
    private val groovy = GroovyScriptEngine()

    fun engine(type: ScriptEngineType): ScriptEngine = when (type) {
        ScriptEngineType.GROOVY -> groovy
    }
}
