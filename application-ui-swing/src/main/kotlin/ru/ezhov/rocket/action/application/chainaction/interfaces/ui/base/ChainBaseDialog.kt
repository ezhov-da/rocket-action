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
        atomicActionService = atomicActionService,
    )

    private val chainConfigurationFrame = ChainConfigurationFrame(
        chainActionExecutorService = chainActionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
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
