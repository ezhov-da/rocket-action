package ru.ezhov.rocket.action.application.engine.application

import ru.ezhov.rocket.action.application.engine.domain.Engine
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.infrastructure.MustacheEngine

object EngineFactory {
    private val mustacheEngine = MustacheEngine()

    fun by(type: EngineType): Engine =
        when (type) {
            EngineType.MUSTACHE -> mustacheEngine
        }
}
