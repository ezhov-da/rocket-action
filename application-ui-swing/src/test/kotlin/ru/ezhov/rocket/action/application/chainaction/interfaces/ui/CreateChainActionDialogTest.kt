package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.mockk
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.ChainActionExecutorImpl
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonAtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonChainActionRepository
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import javax.swing.SwingUtilities
import javax.swing.UIManager


fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val dialog = CreateChainActionDialog(
            ChainActionExecutorService(
                ChainActionExecutorImpl(
                    engineFactory = EngineFactory(),
                    variablesApplication = mockk(),
                    atomicActionService = mockk(),
                ),
                mockk(),
            ),
            ChainActionService(
                JsonChainActionRepository(TestUtilsFactory.objectMapper),
            ),
            AtomicActionService(
                JsonAtomicActionRepository(TestUtilsFactory.objectMapper),
            )
        )
        dialog.isVisible = true
        System.exit(0)
    }
}

internal class CreateChainActionDialogTest
