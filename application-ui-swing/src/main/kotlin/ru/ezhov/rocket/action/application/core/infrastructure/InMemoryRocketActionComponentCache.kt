package ru.ezhov.rocket.action.application.core.infrastructure

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.application.core.domain.RocketActionComponentCache
import java.util.concurrent.ConcurrentHashMap

class InMemoryRocketActionComponentCache : RocketActionComponentCache {
    private val map: MutableMap<String, RocketAction> = ConcurrentHashMap<String, RocketAction>()

    override fun add(id: String, component: RocketAction) {
        map[id] = component
    }

    override fun by(id: String): RocketAction? = map[id]
    override fun byIds(ids: Set<String>): List<RocketAction> =
        map
            .filter { ids.contains(it.key) }
            .values
            .toList()

    override fun all(): List<RocketAction> = map.values.toList()

    override fun handlers(): List<RocketActionHandler> =
        map
            .values
            .mapNotNull { it as? RocketActionHandler }
            .toList()

    override fun handlerBy(id: String): RocketActionHandler? = map[id] as? RocketActionHandler

    override fun clear() {
        map.clear()
    }
}
