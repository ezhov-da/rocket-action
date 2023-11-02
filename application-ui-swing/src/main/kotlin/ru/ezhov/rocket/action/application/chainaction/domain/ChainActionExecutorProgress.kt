package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import java.lang.Exception

interface ChainActionExecutorProgress {
    fun onChainComplete(result: Any?, lastAtomicAction: AtomicAction)

    fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction)

    fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception)
}
