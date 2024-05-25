package ru.ezhov.rocket.action.application.chainaction.domain

/**
 * Used to call from scripts
 */
interface AtomicActionExecutorPublicApi {
    fun execute(alias: String, arg: Any?): Any?
}
