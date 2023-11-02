package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.ActionExecutorImpl
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonAtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonChainActionRepository
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.variables.application.VariablesDto
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class ChainConfigurationFrameTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = ChainConfigurationFrame(
            ActionExecutorService(
                ActionExecutorImpl(
                    EngineFactory(),
                    mockk { every { all() } returns VariablesDto(key = "123", variables = emptyList()) },
                    mockk(),
                ),
                mockk(),
                mockk(),
            ),
            ChainActionService(
                JsonChainActionRepository(TestUtilsFactory.objectMapper),
            ),
            AtomicActionService(
                JsonAtomicActionRepository(TestUtilsFactory.objectMapper),
            )
        )
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }
}
