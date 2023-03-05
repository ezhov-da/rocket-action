package ru.ezhov.rocket.action.application.variables.domain

import ru.ezhov.rocket.action.application.variables.domain.model.Variables

interface VariableRepository {
    fun all(): Variables
    fun save(variables: Variables)
}
