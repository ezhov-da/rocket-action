package ru.ezhov.rocket.action.application.applicationConfiguration.application

import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations
import ru.ezhov.rocket.action.application.applicationConfiguration.infrastructure.JsonFileConfigurationRepository

// TODO ezhov пока не используется
class ConfigurationApplication {
    companion object {
        val INSTANCE = ConfigurationApplication()
    }

    private val configurationRepository = JsonFileConfigurationRepository()

    fun all(): ApplicationConfigurations = configurationRepository.configurations()

    fun save(applicationConfigurations: ApplicationConfigurations) =
        configurationRepository.save(applicationConfigurations)
}
