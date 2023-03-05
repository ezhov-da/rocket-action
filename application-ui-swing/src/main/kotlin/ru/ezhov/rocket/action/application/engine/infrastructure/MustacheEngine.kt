package ru.ezhov.rocket.action.application.engine.infrastructure

import com.github.mustachejava.DefaultMustacheFactory
import ru.ezhov.rocket.action.application.engine.domain.Engine
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import java.io.StringReader
import java.io.StringWriter

class MustacheEngine : Engine {
    override fun execute(template: String, variables: List<EngineVariable>): String {
        val factory = DefaultMustacheFactory()
        val mustache = factory.compile(StringReader(template), "engine")
        val result = StringWriter()

        mustache.execute(result, variables.associate { it.name to it.value }).flush()

        return result.toString()
    }
}
