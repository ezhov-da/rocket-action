package ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.ConfigurationRepository
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationLocationOnScreen
import ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure.model.JsonApplicationConfigurationsDto
import ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure.model.JsonApplicationLocationOnScreenDto
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File

private val logger = KotlinLogging.logger {}

class JsonFileConfigurationRepository : ConfigurationRepository {
    private val filePath =
        GeneralPropertiesRepositoryFactory
            .repository
            .asStringOrNull(UsedPropertiesName.APPLICATION_CONFIGURATION_FILE_REPOSITORY_PATH)
            ?: "./configurations.json"
    private val mapper = ObjectMapper().registerKotlinModule()

    override fun configurations(): ApplicationConfigurations {
        val file = file()
        return if (file.exists()) {
            mapper.readValue(file, JsonApplicationConfigurationsDto::class.java).toApplicationConfigurations()
        } else {
            ApplicationConfigurations()
        }
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
        mapper.writeValue(file, applicationConfigurations.toJsonApplicationConfigurationsDto())
    }
}

private fun ApplicationConfigurations.toJsonApplicationConfigurationsDto(): JsonApplicationConfigurationsDto =
    JsonApplicationConfigurationsDto(
        applicationLocationOnScreen = this.applicationLocationOnScreen?.let {
            JsonApplicationLocationOnScreenDto(
                x = it.x,
                y = it.y,
            )
        }
    )

private fun JsonApplicationConfigurationsDto.toApplicationConfigurations(): ApplicationConfigurations =
    ApplicationConfigurations(
        applicationLocationOnScreen = this.applicationLocationOnScreen?.let {
            ApplicationLocationOnScreen(
                x = it.x,
                y = it.y,
            )
        }
    )
