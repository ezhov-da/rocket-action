package ru.ezhov.rocket.action.infrastructure

import ru.ezhov.rocket.action.domain.RocketActionComponentCache

object RocketActionComponentCacheFactory {
    val cache: RocketActionComponentCache = InMemoryRocketActionComponentCache()
}