package ru.ezhov.rocket.action.application.plugin.context.variables

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.context.variables.VariablesService
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication

@Service
class VariablesServiceImpl(
    private val variablesApplication: VariablesApplication
) : VariablesService {
    override fun variables(): Map<String, String> =
        variablesApplication.all().variables.associate { it.name to it.value }
}
