package ru.ezhov.rocket.action.application.applicationConfiguration.application

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.ConfigurationRepository
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations

@Service
class ConfigurationApplication(
    private val configurationRepository: ConfigurationRepository
) {
    fun all(): ApplicationConfigurations = configurationRepository.configurations()

    fun save(applicationConfigurations: ApplicationConfigurations) =
        configurationRepository.save(applicationConfigurations)
}
