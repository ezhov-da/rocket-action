package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSourceType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.RocketActionPluginDecorator
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.io.File
import java.io.FileFilter
import java.time.Duration
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

@Service
class KotlinPluginLoader(
    private val variablesApplication: VariablesApplication,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
    private val engineFactory: EngineFactory,
) {
    fun plugins(): List<File> =
        File(
            generalPropertiesRepository.asString(
                UsedPropertiesName.KOTLIN_PLUGIN_FOLDER,
                "./kotlin-plugins"
            )
        )
            .apply {
                logger.info { "Directory for load kotlin plugins '${this.absolutePath}'" }
            }
            .listFiles(FileFilter { it.name.endsWith(".kt") })
            ?.toList()
            .orEmpty()

    fun loadPlugin(file: File): RocketActionPluginSpec {
        val from = "From file '${file.absolutePath}'"
        val sourceType = RocketActionPluginSourceType.KOTLIN_SCRIPT

        return try {
            val rocketActionPlugin: RocketActionPlugin
            val ms = measureTimeMillis {
                val executeResult = engineFactory.by(EngineType.KOTLIN)
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
            logger.debug { "Kotlin plugin is loaded from file='${file.absolutePath}' time='$ms'ms" }

            RocketActionPluginSpec.Success(
                rocketActionPlugin = RocketActionPluginDecorator(rocketActionPlugin),
                from = from,
                sourceType = sourceType,
                loadTime = Duration.ofMillis(ms),
            )
        } catch (ex: Exception) {
            logger.error(ex) { "Error when load kotlin plugin from file '${file}'" }
            RocketActionPluginSpec.Failure(from = from, sourceType = sourceType, error = "Error '${ex.message}'")
        }


    }
}
