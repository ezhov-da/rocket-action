package ru.ezhov.rocket.action.application.chainaction.domain

/**
 * Used to call from scripts
 */
interface AtomicActionVariablePublicApi {
    fun updateValue(key: String, value: String)
}
