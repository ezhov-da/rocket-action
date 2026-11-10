package ru.ezhov.rocket.action.api.context.variables

interface VariablesService {
    fun variables(): Map<String, String>
}
