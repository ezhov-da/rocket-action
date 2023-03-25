package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.io.File
import java.io.FileFilter

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

    fun loadPlugin(file: File): RocketActionPlugin? =
        try {
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

            executeResult as RocketActionPlugin
        } catch (ex: Exception) {
            logger.error(ex) { "Error when load groovy plugin from file '${file}'" }
            null
        }
}