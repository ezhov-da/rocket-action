package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.ChainActionExecutorImpl
import ru.ezhov.rocket.action.application.chainaction.infrastructure.InMemoryAtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.infrastructure.InMemoryChainActionRepository
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.variables.application.VariablesDto
import javax.swing.JFrame

internal class ChainConfigurationFrameTest

fun main(args: Array<String>) {
    val frame = ChainConfigurationFrame(
        ChainActionExecutorService(
            ChainActionExecutorImpl(
                EngineFactory(),
                mockk { every { all() } returns VariablesDto(key = "123", variables = emptyList()) })
        ),
        ChainActionService(
            InMemoryChainActionRepository(),
            InMemoryAtomicActionRepository(),
        )
    )
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
}
