package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.ActionExecutorImpl
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonAtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonChainActionRepository
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.variables.application.VariablesDto
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class ChainBasePanelTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________")

        val chainActionService = ChainActionService(JsonChainActionRepository(TestUtilsFactory.objectMapper))
        val atomicActionService = AtomicActionService(JsonAtomicActionRepository(TestUtilsFactory.objectMapper))
        frame.add(
            ChainBasePanel(
                actionExecutorService = ActionExecutorService(
                    actionExecutor = ActionExecutorImpl(
                        engineFactory = EngineFactory(),
                        variablesApplication = mockk {
                            every { all() } returns VariablesDto(
                                key = "",
                                variables = emptyList()
                            )
                        },
                        atomicActionService = atomicActionService
                    ),
                    chainActionService = chainActionService,
                    atomicActionService = atomicActionService,
                ),
                chainActionService = chainActionService,
                atomicActionService = atomicActionService,
                configurationApplication = mockk(),
                searchTextTransformer = mockk(),
                actionSchedulerService = mockk()
            )
        )
        frame.setSize(300, 200)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isAlwaysOnTop = true
        frame.isVisible = true
    }
}
