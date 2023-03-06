package ru.ezhov.rocket.action.application.domain

import ru.ezhov.rocket.action.application.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication

class EngineService {
    fun processWithEngine(settingsModel: SettingsModel): String =
        when (settingsModel.valueType ?: SettingsValueType.PLAIN_TEXT) {
            SettingsValueType.MUSTACHE_TEMPLATE -> EngineFactory.by(EngineType.MUSTACHE)
            else -> null
        }
            ?.let { engine ->
                val variables =
                    VariablesApplication().all()
                        .variables.map { variable ->
                            EngineVariable(
                                name = variable.name,
                                value = variable.value,
                            )

                        }
                engine.execute(settingsModel.value, variables)
            }
            ?: settingsModel.value
}
