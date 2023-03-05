package ru.ezhov.rocket.action.application.variables.domain.model

data class Variable(
    val name: String,
    val value: String,
    val description: String? = null,
    val type: VariableType,
)
