package ru.ezhov.rocket.action.application.variables.domain.importv

import ru.ezhov.rocket.action.application.variables.domain.model.Variable

interface ImportVariablesService {
    fun variables(): List<Variable>
}
