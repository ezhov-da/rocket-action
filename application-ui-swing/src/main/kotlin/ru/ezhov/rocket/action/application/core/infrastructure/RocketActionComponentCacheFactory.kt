package ru.ezhov.rocket.action.application.core.infrastructure

import ru.ezhov.rocket.action.application.core.domain.RocketActionComponentCache

object RocketActionComponentCacheFactory {
    val cache: RocketActionComponentCache = InMemoryRocketActionComponentCache()
}
