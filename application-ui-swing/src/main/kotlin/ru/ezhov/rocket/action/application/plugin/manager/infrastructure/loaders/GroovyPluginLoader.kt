package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSourceType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.RocketActionPluginDecorator
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.io.File
import java.io.FileFilter
import java.time.Duration
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

class GroovyPluginLoader {
    private val variablesApplication: VariablesApplication = VariablesApplication()

    fun plugins(): List<File> =
        File(
            GeneralPropertiesRepositoryFactory.repository.asString(
                UsedPropertiesName.GROOVY_PLUGIN_FOLDER,
                "./groovy-plugins"
            )
        )
            .apply {
                logger.info { "Directory for load groovy plugins '${this.absolutePath}'" }
            }
            .listFiles(FileFilter { it.name.endsWith(".groovy") })
            ?.toList()
            .orEmpty()

    fun loadPlugin(file: File): RocketActionPluginSpec {
        val from = "From file '${file.absolutePath}'"
        val sourceType = RocketActionPluginSourceType.GROOVY_SCRIPT
        return try {
            val rocketActionPlugin: RocketActionPlugin
            val ms = measureTimeMillis {
                val executeResult = EngineFactory
                    .by(EngineType.GROOVY)
                    .execute(
                        template = file.readText(),
                        variables = variablesApplication.all().variables.map {
                            EngineVariable(
                                name = it.name,
                                value = it.value,
                            )
                        })

                rocketActionPlugin = executeResult as RocketActionPlugin

            }
            logger.debug { "Groovy plugin is loaded from file='${file.absolutePath}' time='$ms'ms" }

            RocketActionPluginSpec.Success(
                rocketActionPlugin = RocketActionPluginDecorator(rocketActionPlugin),
                from = from,
                sourceType = sourceType,
                loadTime = Duration.ofMillis(ms),
            )
        } catch (ex: Exception) {
            logger.error(ex) { "Error when load groovy plugin from file '${file}'" }
            RocketActionPluginSpec.Failure(from = from, sourceType = sourceType, error = "Error '${ex.message}'")
        }
    }
}
