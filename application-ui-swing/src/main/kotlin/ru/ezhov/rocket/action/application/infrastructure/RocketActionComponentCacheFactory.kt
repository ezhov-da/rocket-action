package ru.ezhov.rocket.action.application.infrastructure

import ru.ezhov.rocket.action.application.domain.RocketActionComponentCache

object RocketActionComponentCacheFactory {
    val cache: RocketActionComponentCache = InMemoryRocketActionComponentCache()
}