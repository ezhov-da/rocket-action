package ru.ezhov.rocket.action.configuration.domain

import ru.ezhov.rocket.action.api.RocketActionConfiguration

interface RocketActionConfigurationRepository {
    fun all(): List<RocketActionConfiguration>
    fun by(type: String): RocketActionConfiguration?
}