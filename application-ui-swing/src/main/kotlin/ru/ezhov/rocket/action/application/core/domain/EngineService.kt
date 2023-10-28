package ru.ezhov.rocket.action.application.core.domain

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication

@Service
class EngineService(
    private val variablesApplication: VariablesApplication,
    private val engineFactory: EngineFactory,
) {
    fun processWithEngine(settingsModel: SettingsModel): Any =
        settingsModel
            .valueType
            ?.let { type ->
                when (type) {
                    SettingsValueType.GROOVY_TEMPLATE -> EngineType.GROOVY
                    SettingsValueType.MUSTACHE_TEMPLATE -> EngineType.MUSTACHE
                    SettingsValueType.PLAIN_TEXT -> null
                }
            }?.let { engineFactory.by(it) }
            ?.let { engine ->
                val variables =
                    variablesApplication.all()
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
