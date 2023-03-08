package ru.ezhov.rocket.action.application.plugin.context.variables

import ru.ezhov.rocket.action.api.context.variables.VariablesService
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication

class VariablesServiceImpl : VariablesService {
    private val variablesApplication: VariablesApplication = VariablesApplication()

    override fun variables(): Map<String, String> =
        variablesApplication.all().variables.associate { it.name to it.value }
}
