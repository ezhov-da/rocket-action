package ru.ezhov.rocket.action.application.infrastructure

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.configuration.ui.create.NewRocketActionSettings

class MutableRocketActionSettings(
    private val id: String,
    private val type: RocketActionType,
    private val settings: MutableMap<RocketActionConfigurationPropertyKey, String>,
    private val actions: MutableList<RocketActionSettings> = ArrayList()
) : RocketActionSettings {

    override fun id(): String = id

    override fun type(): RocketActionType = type

    override fun settings(): MutableMap<RocketActionConfigurationPropertyKey, String> = settings

    override fun actions(): List<RocketActionSettings> = actions

    fun add(key: RocketActionConfigurationPropertyKey, value: String) {
        settings[key] = value
    }

    fun add(settings: MutableRocketActionSettings) {
        actions.add(settings)
    }

    fun copy(configuration: RocketActionConfiguration): MutableRocketActionSettings {
        val new = NewRocketActionSettings(
            configuration = configuration,
            type = type,
            settings = settings.toMap(),
            actions = actions.toList(),
        )
        return MutableRocketActionSettings(
            new.id(),
            type = new.type(),
            settings = new.settings().toMutableMap(),
            actions = new.actions().toMutableList(),
        )
    }
}
