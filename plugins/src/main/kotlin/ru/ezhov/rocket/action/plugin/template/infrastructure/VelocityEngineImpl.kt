package ru.ezhov.rocket.action.plugin.template.infrastructure

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import ru.ezhov.rocket.action.plugin.template.domain.Engine
import java.io.StringWriter

class VelocityEngineImpl : Engine {
    override fun apply(template: String, values: Map<String, String>): String {
        // Initialize the engine.
        val engine = VelocityEngine()
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute")
        engine.setProperty(Velocity.RESOURCE_LOADER, "string")
        engine.addProperty("string.resource.loader.class", StringResourceLoader::class.java.name)
        engine.addProperty("string.resource.loader.repository.static", "false")
        engine.init()

        // Initialize my template repository. You can replace the "Hello $w" with your String.
        val repo = engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepository
        repo.putStringResource("template", templateWithoutWorlds(template))

        // Set parameters for my template.
        val context = VelocityContext()
        values.forEach { (key: String, value: String) -> context.put(key, value) }

        // Get and merge the template with my parameters.
        val templateVelocity = engine.getTemplate("template", "UTF-8")
        val writer = StringWriter()
        templateVelocity.merge(context, writer)
        return writer.toString()
    }

    companion object {
        private const val PREFIX_VARIABLE = "_>"
    }

    override fun words(text: String): List<String> =
        text
            .lines()
            .filter { it.startsWith(PREFIX_VARIABLE) }
            .map { it.substringAfter(PREFIX_VARIABLE).trim() }

    private fun templateWithoutWorlds(text: String): String =
        text
            .lines()
            .filter { !it.startsWith(PREFIX_VARIABLE) }
            .joinToString(separator = "")
}
