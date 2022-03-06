package ru.ezhov.rocket.action.infrastructure

import ru.ezhov.rocket.action.domain.RocketActionUiRepository

object RocketActionUiRepositoryFactory {
    val repository: RocketActionUiRepository = ReflectionRocketActionUiRepository()
}