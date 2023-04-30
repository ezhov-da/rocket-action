package ru.ezhov.rocket.action.application.domain.model

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.domain.EngineService
import java.util.*

private val logger = KotlinLogging.logger { }

data class RocketActionSettingsModel(
    val id: String,
    val type: String,
    val settings: List<SettingsModel>,
    val actions: List<RocketActionSettingsModel>,
    val tags: List<String>,
) {
    fun to(): RocketActionSettings = object : RocketActionSettings {
        override fun id(): String = id

        override fun type(): RocketActionType = RocketActionType { type }

        override fun settings(): Map<String, String> =
            try {
                settings.associate { set ->
                    // TODO ezhov возможность для оптимизации
                    val resultVal = EngineService().processWithEngine(set).toString()
                    set.name to resultVal
                }
            } catch (ex: Exception) {
                logger.warn(ex) { "Error when get settings for action with id='$id'" }
                emptyMap()
            }

        override fun actions(): List<RocketActionSettings> = actions.map { it.to() }
    }

    companion object {
        fun generateId() = UUID.randomUUID().toString()
    }
}
