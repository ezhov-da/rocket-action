package ru.ezhov.rocket.action.application.engine.domain

import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable

interface Engine {
    fun execute(template: String, variables: List<EngineVariable>): String
}
