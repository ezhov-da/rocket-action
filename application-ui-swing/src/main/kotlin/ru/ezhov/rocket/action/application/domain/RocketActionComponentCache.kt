package ru.ezhov.rocket.action.application.domain

import ru.ezhov.rocket.action.api.RocketAction

interface RocketActionComponentCache {
    fun add(id: String, component: RocketAction)

    fun by(id: String): RocketAction?

    fun all(): List<RocketAction>

    fun clear()
}