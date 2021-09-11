package ru.ezhov.rocket.action.infrastructure

import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType

class MutableRocketActionSettings : RocketActionSettings {
    private var id: String
    private var type: RocketActionType
    private var settings: MutableMap<String, String>
    private var actions: MutableList<RocketActionSettings>

    constructor(
            id: String,
            type: RocketActionType,
            settings: MutableMap<String, String>,
            actions: MutableList<RocketActionSettings>
    ) {
        this.id = id
        this.type = type
        this.settings = settings
        this.actions = actions
    }

    constructor(
            id: String,
            type: RocketActionType,
            settings: MutableMap<String, String>
    ) {
        this.id = id
        this.type = type
        this.settings = settings
        actions = ArrayList()
    }

    override fun id(): String = id

    override fun type(): RocketActionType = type

    override fun settings(): MutableMap<String, String> = settings

    override fun actions(): List<RocketActionSettings> = actions

    fun add(key: String, value: String) {
        settings[key] = value
    }

    fun add(settings: MutableRocketActionSettings) {
        actions.add(settings)
    }
}