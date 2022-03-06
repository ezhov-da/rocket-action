package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import java.util.UUID

class NewRocketActionSettings(
    val configuration: RocketActionConfiguration,
    private val type: RocketActionType,
    private val settings: Map<RocketActionConfigurationPropertyKey, String>,
    private val actions: List<RocketActionSettings> = emptyList()
) : RocketActionSettings {
    private val id: String = UUID.randomUUID().toString()

    override fun id(): String = id

    override fun type(): RocketActionType = type

    override fun settings(): Map<RocketActionConfigurationPropertyKey, String> = settings

    override fun actions(): List<RocketActionSettings> = actions
}