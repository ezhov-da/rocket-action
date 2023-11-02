package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.ChainBaseDialog

@Service
class ChainBaseDialogBuilder(
    private val actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) {
    fun build(): ChainBaseDialog =
        ChainBaseDialog(
            actionExecutorService = actionExecutorService,
            chainActionService = chainActionService,
            atomicActionService = atomicActionService,
        )
}
