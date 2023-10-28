package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import java.lang.Exception

interface ChainActionExecutorProgress {
    fun complete(result: Any?)

    fun success(atomicAction: AtomicAction)

    fun failure(id: String, atomicAction: AtomicAction?, ex: Exception)
}
