package ru.ezhov.rocket.action.application.plugin.manager.domain

import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionType

interface RocketActionPluginRepository {
    fun all(): List<RocketActionPluginSpec>

    fun by(type: String): RocketActionPluginSpec?
}
