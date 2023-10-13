package ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.ConfigurationRepository
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations
import ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure.model.JsonApplicationConfigurationsDto
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File

private val logger = KotlinLogging.logger {}

private const val DEFAULT_VARIABLES_KEY: String = "314dcf4c-e12b-11ed-b5ea-0242ac120002"

@Component
class JsonFileConfigurationRepository(
    generalPropertiesRepository: GeneralPropertiesRepository
) : ConfigurationRepository {
    private var cachedApplicationConfigurations: ApplicationConfigurations? = null
    private val filePath =
        generalPropertiesRepository
            .asStringOrNull(UsedPropertiesName.APPLICATION_CONFIGURATION_FILE_REPOSITORY_PATH)
            ?: "./configurations.json"
    private val mapper = ObjectMapper().registerKotlinModule()

    override fun configurations(): ApplicationConfigurations {
        if (cachedApplicationConfigurations == null) {
            val file = file()
            cachedApplicationConfigurations = if (file.exists()) {
                logger.info { "Read application configuration from file '$file'" }
                mapper.readValue(file, JsonApplicationConfigurationsDto::class.java).toApplicationConfigurations()
            } else {
                logger.info { "Application configurations file '$file' does not exists. Return default configuration" }
                ApplicationConfigurations(
                    variablesKey = DEFAULT_VARIABLES_KEY,
                )
            }
        }

        return cachedApplicationConfigurations!!
    }

    private fun file(): File {
        val file = File(filePath)
        if (!file.exists()) {
            file.parentFile.mkdirs()

        }

        return file
    }

    override fun save(applicationConfigurations: ApplicationConfigurations) {
        val file = file()
        mapper.writerWithDefaultPrettyPrinter()
            .writeValue(file, applicationConfigurations.toJsonApplicationConfigurationsDto())

        cachedApplicationConfigurations = applicationConfigurations
    }
}

private fun ApplicationConfigurations.toJsonApplicationConfigurationsDto(): JsonApplicationConfigurationsDto =
    JsonApplicationConfigurationsDto(
        variablesKey = this.variablesKey
    )

private fun JsonApplicationConfigurationsDto.toApplicationConfigurations(): ApplicationConfigurations =
    ApplicationConfigurations(
        variablesKey = this.variablesKey ?: DEFAULT_VARIABLES_KEY
    )
