package ru.ezhov.rocket.action.application.core.domain.model

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
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
    /**
     * Applies the reload scope: the settings of an action are evaluated once per reload
     * and every view of the same action reuses the result.
     */
    fun to(scope: ActionReloadScope): RocketActionSettings =
        object : RocketActionSettings {
            private val evaluatedSettings: Map<String, String> by lazy {
                scope.settingsOf(id) {
                    try {
                        settings.associate { set ->
                            if (set.valueType == SettingsValueType.GROOVY_TEMPLATE ||
                                set.valueType == SettingsValueType.MUSTACHE_TEMPLATE
                            ) {
                                scope.evaluated()
                            }
                            val resultVal = scope.evaluate(set).toString()
                            set.name to resultVal
                        }
                    } catch (ex: Exception) {
                        logger.warn(ex) { "Error when get settings for action with id='$id'" }
                        emptyMap()
                    }
                }
            }

            private val evaluatedActions: List<RocketActionSettings> by lazy { actions.map { it.to(scope) } }

            override fun id(): String = id

            override fun type(): RocketActionType = RocketActionType { type }

            override fun settings(): Map<String, String> = evaluatedSettings

            override fun actions(): List<RocketActionSettings> = evaluatedActions
        }

    /**
     * Compatibility overload for callers that do not own a reload (configuration UI, old format reader).
     * It opens a short-lived scope that lives as long as the returned view.
     */
    fun to(engineService: EngineService): RocketActionSettings {
        val scope = ActionReloadScope(engineService::processWithEngine)
        return to(scope)
    }

    companion object {
        fun generateId() = UUID.randomUUID().toString()
    }
}
