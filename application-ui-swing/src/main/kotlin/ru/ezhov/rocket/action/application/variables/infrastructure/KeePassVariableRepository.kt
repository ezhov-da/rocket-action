package ru.ezhov.rocket.action.application.variables.infrastructure

import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.VariablesManager
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.VariablesManagerType
import ru.ezhov.rocket.action.application.variables.application.VariableDto
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.infrastructure.manager.KeePassManagerRepository

class KeePassVariableRepository(
    private val variables: List<VariableDto>,
    private val managers: List<VariablesManager>,
) {
    fun all(): List<Variable> {
        val keePassManager = managers
            .firstOrNull { it.type == VariablesManagerType.KEE_PASS }
            ?.let { it as VariablesManager.KeePassVariablesManager }

        if (keePassManager == null) {
            return emptyList()
        }

        return variables
            .firstOrNull { it.name == keePassManager.passwordVariableName }
            ?.let { v -> KeePassManagerRepository().variables(v.value, keePassManager) }
            .orEmpty()
    }
}
