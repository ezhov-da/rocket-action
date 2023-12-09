package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.ChainBaseDialog
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository

@Service
class ChainBaseDialogBuilder(
    private val actionExecutor: ActionExecutor,
    private val actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    private val configurationApplication: ConfigurationApplication,
    private val iconRepository: IconRepository,
) {
    fun build(): ChainBaseDialog =
        ChainBaseDialog(
            actionExecutor = actionExecutor,
            actionExecutorService = actionExecutorService,
            chainActionService = chainActionService,
            atomicActionService = atomicActionService,
            configurationApplication = configurationApplication,
            iconRepository = iconRepository,
        )
}
