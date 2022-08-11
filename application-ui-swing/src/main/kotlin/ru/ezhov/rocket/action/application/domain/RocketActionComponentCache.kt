package ru.ezhov.rocket.action.application.domain

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.handler.RocketActionHandler

interface RocketActionComponentCache {
    fun add(id: String, component: RocketAction)

    fun by(id: String): RocketAction?

    fun all(): List<RocketAction>

    fun handlers(): List<RocketActionHandler>

    fun handlerBy(id: String): RocketActionHandler?

    fun clear()
}
