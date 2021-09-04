package ru.ezhov.rocket.action.domain

import ru.ezhov.rocket.action.api.Action

interface RocketActionComponentCache {
    fun add(id: String, component: Action)

    fun by(id: String): Action?

    fun all(): List<Action>

    fun clear()
}