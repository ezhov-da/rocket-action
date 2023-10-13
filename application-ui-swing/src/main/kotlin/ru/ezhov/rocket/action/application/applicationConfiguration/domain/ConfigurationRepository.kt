package ru.ezhov.rocket.action.application.applicationConfiguration.domain

import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations

interface ConfigurationRepository {
    fun configurations(): ApplicationConfigurations
    fun save(applicationConfigurations: ApplicationConfigurations)
}
