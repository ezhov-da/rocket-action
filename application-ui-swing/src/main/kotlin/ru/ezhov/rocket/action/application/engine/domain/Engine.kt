package ru.ezhov.rocket.action.application.engine.domain

import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable

interface Engine {
    companion object{
        const val VARIABLES_NAME = "_VARIABLES"
    }

    fun execute(template: String, variables: List<EngineVariable>): Any?
}
