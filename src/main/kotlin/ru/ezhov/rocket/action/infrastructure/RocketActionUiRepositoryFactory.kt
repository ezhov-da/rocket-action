package ru.ezhov.rocket.action.infrastructure

import ru.ezhov.rocket.action.domain.RocketActionUiRepository

object RocketActionUiRepositoryFactory {
    val repository: RocketActionUiRepository

    init {
        val reflectionRocketActionUiRepository = ReflectionRocketActionUiRepository()
        reflectionRocketActionUiRepository.load()
        repository = reflectionRocketActionUiRepository
    }
}