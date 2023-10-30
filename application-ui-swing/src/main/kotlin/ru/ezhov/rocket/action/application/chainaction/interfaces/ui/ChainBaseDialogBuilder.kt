package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.ChainBaseDialog

@Service
class ChainBaseDialogBuilder(
    private val chainActionExecutorService: ChainActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) {
    fun build(): ChainBaseDialog =
        ChainBaseDialog(
            chainActionExecutorService = chainActionExecutorService,
            chainActionService = chainActionService,
            atomicActionService = atomicActionService,
        )
}
