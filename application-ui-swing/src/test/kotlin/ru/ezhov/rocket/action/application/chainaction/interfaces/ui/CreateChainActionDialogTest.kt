package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.mockk
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.ChainActionExecutorImpl
import ru.ezhov.rocket.action.application.chainaction.infrastructure.InMemoryAtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.infrastructure.InMemoryChainActionRepository
import ru.ezhov.rocket.action.application.engine.application.EngineFactory


fun main(args: Array<String>) {
    val dialog = CreateChainActionDialog(
        ChainActionExecutorService(ChainActionExecutorImpl(EngineFactory(), mockk())),
        ChainActionService(
            InMemoryChainActionRepository(),
            InMemoryAtomicActionRepository(),
        )
    )
    dialog.isVisible = true
    System.exit(0)
}

internal class CreateChainActionDialogTest
