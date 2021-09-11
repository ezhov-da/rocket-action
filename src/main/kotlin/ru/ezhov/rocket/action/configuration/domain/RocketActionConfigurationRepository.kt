package ru.ezhov.rocket.action.configuration.domain

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionType

interface RocketActionConfigurationRepository {
    fun all(): List<RocketActionConfiguration>
    fun by(type: RocketActionType): RocketActionConfiguration?
}