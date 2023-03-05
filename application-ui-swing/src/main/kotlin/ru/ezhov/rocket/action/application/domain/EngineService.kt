package ru.ezhov.rocket.action.application.domain

import ru.ezhov.rocket.action.application.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication

class EngineService {
    fun processWithEngine(settingsModel: SettingsModel): String {
        val engine = when (settingsModel.valueType ?: SettingsValueType.PLAIN_TEXT) {
            SettingsValueType.MUSTACHE_TEMPLATE -> EngineFactory.by(EngineType.MUSTACHE)
            else -> null
        }

        val variables =
            VariablesApplication().all().variables.map {
                EngineVariable(
                    name = it.name,
                    value = it.value,
                )
            }

        return engine
            ?.execute(settingsModel.value, variables)
            ?: settingsModel.value
    }
}
