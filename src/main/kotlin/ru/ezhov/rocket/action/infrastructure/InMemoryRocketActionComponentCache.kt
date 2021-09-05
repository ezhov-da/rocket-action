package ru.ezhov.rocket.action.infrastructure

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.domain.RocketActionComponentCache
import java.util.concurrent.ConcurrentHashMap

class InMemoryRocketActionComponentCache : RocketActionComponentCache {
    private val map: MutableMap<String, RocketAction> = ConcurrentHashMap<String, RocketAction>()

    override fun add(id: String, component: RocketAction) {
        map[id] = component
    }

    override fun by(id: String): RocketAction? = map[id]

    override fun all(): List<RocketAction> = map.values.toList()

    override fun clear() {
        map.clear()
    }
}