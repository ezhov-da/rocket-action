package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import java.lang.Exception

interface ChainActionExecutorProgress {
    fun complete(result: Any?)

    fun success(orderId: String, atomicAction: AtomicAction)

    fun failure(orderId: String, atomicAction: AtomicAction?, ex: Exception)
}
