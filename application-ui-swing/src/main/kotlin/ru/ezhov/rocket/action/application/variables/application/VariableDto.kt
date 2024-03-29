package ru.ezhov.rocket.action.application.variables.application

import ru.ezhov.rocket.action.application.variables.domain.model.VariableType

data class VariableDto(
    val name: String,
    val value: String,
    val description: String? = null,
    val type: VariableType,
)
