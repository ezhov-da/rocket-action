package ru.ezhov.rocket.action.application.plugin.manager.domain

import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionType

interface RocketActionPluginRepository {
    fun all(): List<RocketActionPlugin>

    fun by(type: RocketActionType): RocketActionPlugin?
}
