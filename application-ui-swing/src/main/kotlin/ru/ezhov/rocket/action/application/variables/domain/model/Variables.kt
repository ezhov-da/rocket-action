package ru.ezhov.rocket.action.application.variables.domain.model

data class Variables(
    val variables: List<Variable>
) {
    companion object {
        val EMPTY = Variables(emptyList())
    }
}
