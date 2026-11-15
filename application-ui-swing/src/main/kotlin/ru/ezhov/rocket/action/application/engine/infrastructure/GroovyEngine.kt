package ru.ezhov.rocket.action.application.engine.infrastructure

import groovy.lang.Binding
import groovy.lang.Script
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.engine.domain.Engine
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

class GroovyEngine(
    private val scriptClassCache: GroovyScriptClassCache = GroovyScriptClassCache(),
) : Engine {

    override fun execute(template: String, variables: List<EngineVariable>): Any? {
        var result: Any? = null
        val time = measureTimeMillis {
            val binding = Binding()
            variables.forEach { binding.setProperty(it.name, it.value) }
            binding.setProperty(Engine.VARIABLES_NAME, variables.associate { it.name to it.value })

            val script = scriptClassCache.classFor(template)
                .getDeclaredConstructor()
                .newInstance() as Script
            script.binding = binding
            result = script.run()
        }

        logger.debug {
            "Time execute groovy script '$time'ms. " +
                "Template='$template', variables=${variables.associate { it.name to it.value?.toString()?.take(3) }}"
        }

        return result
    }
}
