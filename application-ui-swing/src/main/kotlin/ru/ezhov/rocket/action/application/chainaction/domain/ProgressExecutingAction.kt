package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction

interface ProgressExecutingAction {
    fun onComplete(result: Any?, lastAtomicAction: AtomicAction)

    fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction)

    fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception)
}
