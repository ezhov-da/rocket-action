package ru.ezhov.rocket.action.application.variables.application

import ru.ezhov.rocket.action.application.variables.domain.model.Variables
import ru.ezhov.rocket.action.application.variables.infrastructure.JsonFileVariableRepository

class VariablesApplication {
    private val variableRepository = JsonFileVariableRepository()

    fun all(): Variables = variableRepository.all()

    fun save(variables: Variables) = variableRepository.save(variables)
}
