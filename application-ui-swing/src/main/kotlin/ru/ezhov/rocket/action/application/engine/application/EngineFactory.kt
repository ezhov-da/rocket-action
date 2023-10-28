package ru.ezhov.rocket.action.application.engine.application

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.engine.domain.Engine
import ru.ezhov.rocket.action.application.engine.domain.model.EngineType
import ru.ezhov.rocket.action.application.engine.infrastructure.GroovyEngine
import ru.ezhov.rocket.action.application.engine.infrastructure.KotlinEngine
import ru.ezhov.rocket.action.application.engine.infrastructure.MustacheEngine

@Service
class EngineFactory {
    private val mustacheEngine = MustacheEngine()
    private val groovyEngine = GroovyEngine()
    private val kotlinEngine = KotlinEngine()

    fun by(type: EngineType): Engine =
        when (type) {
            EngineType.MUSTACHE -> mustacheEngine
            EngineType.GROOVY -> groovyEngine
            EngineType.KOTLIN -> kotlinEngine
        }
}
