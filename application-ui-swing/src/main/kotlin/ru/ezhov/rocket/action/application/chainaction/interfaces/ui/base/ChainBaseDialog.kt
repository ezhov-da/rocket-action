package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainConfigurationFrame
import java.awt.BorderLayout
import javax.swing.JDialog

class ChainBaseDialog(
    chainActionExecutorService: ChainActionExecutorService,
    chainActionService: ChainActionService,
    atomicActionService: AtomicActionService,
) : JDialog() {

    private val chainBasePanel: ChainBasePanel = ChainBasePanel(
        movableComponent = this,
        chainActionExecutorService = chainActionExecutorService,
        chainActionService = chainActionService,
    )

    private val chainConfigurationFrame = ChainConfigurationFrame(
        chainActionExecutorService = chainActionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
    )

    init {
        setSize(200, 140)
        isAlwaysOnTop = true
        isUndecorated = true
        add(chainBasePanel, BorderLayout.CENTER)
        setLocationRelativeTo(null)
    }
}
