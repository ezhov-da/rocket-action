package ru.ezhov.rocket.action.application.chainaction.infrastructure

import ru.ezhov.rocket.action.application.chainaction.domain.AtomicActionVariablePublicApi
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication

class AtomicActionVariablePublicApiImpl(
    private val variablesApplication: VariablesApplication,
) : AtomicActionVariablePublicApi {
    override fun updateValue(key: String, value: String) {
        variablesApplication.updateVariable(key, value)
    }
}
