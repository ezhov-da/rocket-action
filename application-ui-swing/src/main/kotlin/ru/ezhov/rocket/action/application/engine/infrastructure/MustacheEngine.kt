package ru.ezhov.rocket.action.application.engine.infrastructure

import com.github.mustachejava.DefaultMustacheFactory
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.engine.domain.Engine
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import java.io.StringReader
import java.io.StringWriter
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

class MustacheEngine : Engine {
    override fun execute(template: String, variables: List<EngineVariable>): Any {
        val result: StringWriter
        val time = measureTimeMillis {
            val factory = DefaultMustacheFactory()
            val mustache = factory.compile(StringReader(template), "engine")
            result = StringWriter()
            mustache.execute(result, variables.associate { it.name to it.value }).flush()
        }

        logger.debug {
            "Time execute mustache script '$time'ms. " +
                "Template='$template', variables=${variables.associate { it.name to it.value }}"
        }

        return result
    }
}
