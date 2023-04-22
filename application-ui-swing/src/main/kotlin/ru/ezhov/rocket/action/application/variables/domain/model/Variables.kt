package ru.ezhov.rocket.action.application.variables.domain.model

data class Variables(
    val encryption: Encryption? = null,
    val variables: List<Variable>
) {

    companion object {
        val EMPTY = Variables(variables = emptyList())
    }
}
