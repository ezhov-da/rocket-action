package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainConfigurationFrame
import java.awt.BorderLayout
import javax.swing.JDialog

class ChainBaseDialog(
    actionExecutor: ActionExecutor,
    actionExecutorService: ActionExecutorService,
    chainActionService: ChainActionService,
    atomicActionService: AtomicActionService,
) : JDialog() {

    private val chainBasePanel: ChainBasePanel = ChainBasePanel(
        movableComponent = this,
        actionExecutorService = actionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
    )

    private val chainConfigurationFrame = ChainConfigurationFrame(
        actionExecutorService = actionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
        actionExecutor = actionExecutor,
    )

    init {
        setSize(200, 120)
        isAlwaysOnTop = true
        isUndecorated = true
        opacity = 0.7F // TODO ezhov test
        add(chainBasePanel, BorderLayout.CENTER)
        setLocationRelativeTo(null)
    }

    fun showDialog() {
        isVisible = true
    }
}
