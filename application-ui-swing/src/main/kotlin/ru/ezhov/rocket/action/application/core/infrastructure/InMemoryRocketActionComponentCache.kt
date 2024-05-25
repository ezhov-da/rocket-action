package ru.ezhov.rocket.action.application.core.infrastructure

import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.application.core.domain.RocketActionComponentCache
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCached
import java.util.concurrent.ConcurrentHashMap

class InMemoryRocketActionComponentCache : RocketActionComponentCache {
    private val map: MutableMap<String, RocketActionCached> = ConcurrentHashMap<String, RocketActionCached>()

    override fun add(id: String, action: RocketActionCached) {
        map[id] = action
    }

    override fun by(id: String): RocketActionCached? = map[id]
    override fun byIds(ids: Set<String>): List<RocketActionCached> =
        map
            .filter { ids.contains(it.key) }
            .values
            .toList()

    override fun all(): List<RocketActionCached> = map.values.toList()

    override fun handlers(): List<RocketActionHandler> =
        map
            .values
            .mapNotNull { it.origin as? RocketActionHandler }
            .toList()

    override fun handlerBy(id: String): RocketActionHandler? = map[id]?.origin as? RocketActionHandler

    override fun clear() {
        map.clear()
    }
}
