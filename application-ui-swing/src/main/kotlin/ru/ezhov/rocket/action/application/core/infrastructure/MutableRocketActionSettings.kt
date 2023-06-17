package ru.ezhov.rocket.action.application.core.infrastructure

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel

private val logger = KotlinLogging.logger { }

class MutableRocketActionSettings(
    val id: String,
    val type: String,
    val settings: MutableList<SettingsModel>,
    val actions: MutableList<MutableRocketActionSettings> = mutableListOf(),
    val tags: List<String>,
) {

    fun to(): RocketActionSettings = object : RocketActionSettings {
        override fun id(): String = id

        override fun type(): RocketActionType = RocketActionType { type }

        override fun settings(): Map<String, String> =
            try {
                settings.associate { set ->
                    // TODO ezhov opportunity for optimization
                    val resultVal = EngineService().processWithEngine(set).toString()
                    set.name to resultVal
                }
            } catch (ex: Exception) {
                logger.warn(ex) { "Error when get settings for action with id='$id'" }
                emptyMap()
            }

        override fun actions(): List<RocketActionSettings> = actions.map { it.to() }
    }

    fun copy(source: MutableRocketActionSettings): MutableRocketActionSettings =
        MutableRocketActionSettings(
            id = RocketActionSettingsModel.generateId(),
            type = source.type,
            settings = settings.map { it.copy() }.toMutableList(),
            actions = mutableListOf(),
            tags = source.tags
        )

    fun toModel(): RocketActionSettingsModel = RocketActionSettingsModel(
        id = this.id,
        type = this.type,
        settings = this.settings.toList(),
        actions = this.actions.map { it.toModel() },
        tags = this.tags,
    )

    companion object {
        fun from(model: RocketActionSettingsModel): MutableRocketActionSettings =
            MutableRocketActionSettings(
                id = model.id,
                type = model.type,
                settings = model.settings.toMutableList(),
                actions = model.actions.map { from(it) }.toMutableList(),
                tags = model.tags,
            )
    }
}
