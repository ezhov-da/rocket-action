package ru.ezhov.rocket.action.application.variables.application

data class VariablesDto(
    val key: String,
    val variables: List<VariableDto>,
)
