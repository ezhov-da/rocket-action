package ru.ezhov.rocket.action.configuration.infrastructure

import ru.ezhov.rocket.action.configuration.domain.RocketActionConfigurationRepository

object RocketActionConfigurationRepositoryFactory {
    val repository: RocketActionConfigurationRepository

    init {
        val reflectionRocketActionConfigurationRepository = ReflectionRocketActionConfigurationRepository()
        reflectionRocketActionConfigurationRepository.load()
        repository = reflectionRocketActionConfigurationRepository
    }
}