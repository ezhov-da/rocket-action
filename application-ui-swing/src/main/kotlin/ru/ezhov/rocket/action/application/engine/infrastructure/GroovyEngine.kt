package ru.ezhov.rocket.action.application.engine.infrastructure

import groovy.lang.Binding
import groovy.lang.GroovyShell
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.engine.domain.Engine
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

class GroovyEngine : Engine {

    companion object {
        private const val VARIABLES_NAME = "_variables"
    }

    override fun execute(template: String, variables: List<EngineVariable>): Any? {
        val result: Any?
        val time = measureTimeMillis {
            val sharedData = Binding()
            val groovyShell = GroovyShell(sharedData)
            variables.forEach { sharedData.setProperty(it.name, it.value) }
            sharedData.setProperty(VARIABLES_NAME, variables.associate { it.name to it.value })
            result = groovyShell.evaluate(template)

        }

        logger.debug {
            "Time execute groovy script '$time'ms. " +
                "Template='$template', variables=${variables.associate { it.name to it.value }}"
        }

        return result
    }
}
