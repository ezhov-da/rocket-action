package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionConfiguration

interface RocketActionConfigurationRepository {
    fun load()
    fun all(): List<RocketActionConfiguration>
    fun by(type: String): RocketActionConfiguration?
}