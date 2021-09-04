package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionSettings
import java.util.*

class NewRocketActionSettings(
        private val type: String,
        private val settings: Map<String, String>,
        private val actions: List<RocketActionSettings> = emptyList()
) : RocketActionSettings {
    private val id: String = UUID.randomUUID().toString()

    override fun id(): String = id

    override fun type(): String {
        return type
    }

    override fun settings(): Map<String, String> {
        return settings
    }

    override fun actions(): List<RocketActionSettings> {
        return actions
    }

}