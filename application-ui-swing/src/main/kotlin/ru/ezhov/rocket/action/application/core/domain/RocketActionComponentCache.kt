package ru.ezhov.rocket.action.application.core.domain

import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCached

interface RocketActionComponentCache {
    fun add(id: String, action: RocketActionCached)

    fun by(id: String): RocketActionCached?

    fun byIds(ids: Set<String>): List<RocketActionCached>

    fun all(): List<RocketActionCached>

    fun handlers(): List<RocketActionHandler>

    fun handlerBy(id: String): RocketActionHandler?

    fun clear()
}
