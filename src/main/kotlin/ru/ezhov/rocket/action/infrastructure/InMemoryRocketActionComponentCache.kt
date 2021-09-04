package ru.ezhov.rocket.action.infrastructure

import ru.ezhov.rocket.action.api.Action
import ru.ezhov.rocket.action.domain.RocketActionComponentCache
import java.util.concurrent.ConcurrentHashMap

class InMemoryRocketActionComponentCache : RocketActionComponentCache {
    private val map: MutableMap<String, Action> = ConcurrentHashMap<String, Action>()

    override fun add(id: String, component: Action) {
        map[id] = component
    }

    override fun by(id: String): Action? = map[id]

    override fun all(): List<Action> = map.values.toList()

    override fun clear() {
        map.clear()
    }
}