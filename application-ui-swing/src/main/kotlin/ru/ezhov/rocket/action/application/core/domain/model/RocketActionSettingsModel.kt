package ru.ezhov.rocket.action.application.core.domain.model

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.core.domain.EngineService
import java.util.*

private val logger = KotlinLogging.logger { }

data class RocketActionSettingsModel(
    // TODO ezhov change to val
    var id: String,
    var type: String,
    var settings: List<SettingsModel>,
    var actions: List<RocketActionSettingsModel>,
    var tags: List<String>,
) {
    fun to(engineService: EngineService): RocketActionSettings =
        object : RocketActionSettings {
            override fun id(): String = id

            override fun type(): RocketActionType = RocketActionType { type }

            override fun settings(): Map<String, String> =
                try {
                    settings.associate { set ->
                        // TODO ezhov opportunity for optimization
                        val resultVal = engineService.processWithEngine(set).toString()
                        set.name to resultVal
                    }
                } catch (ex: Exception) {
                    logger.warn(ex) { "Error when get settings for action with id='$id'" }
                    emptyMap()
                }

            override fun actions(): List<RocketActionSettings> = actions.map { it.to(engineService) }
        }

    companion object {
        fun generateId() = UUID.randomUUID().toString()
    }
}
