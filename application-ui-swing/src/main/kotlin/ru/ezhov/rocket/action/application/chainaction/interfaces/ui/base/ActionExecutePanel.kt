package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import mu.KotlinLogging
import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.util.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

private val logger = KotlinLogging.logger {}

class ActionExecutePanel(
    actionExecutorService: ActionExecutorService
) : JPanel(MigLayout(/*"debug"*/"insets 0 0 0 0")) {

    private val infoLabel = JLabel("<html><b>Save before test</b>")
    private val textFiled = TextFieldWithText("Input value")
    private val doTestButton = JButton("Do test")
    private val actionExecuteStatusPanel = ActionExecuteStatusPanel(actionExecutorService).apply { isVisible = false }

    private val currentTimer: Timer = Timer()

    private var currentAction: Action? = null

    init {
        add(infoLabel, "grow, push, span, wrap")
        add(textFiled, "grow, push")
        add(doTestButton, "wrap")
        add(actionExecuteStatusPanel, "hmax 6, width max, hidemode 2, span")

        doTestButton.addActionListener {
            when (currentAction) {
                null -> {
                    logger.warn { "Action not set for test" }
                }

                else -> {
                    actionExecuteStatusPanel.isVisible = true

                    actionExecuteStatusPanel.executeChain(input = textFiled.text, action = currentAction!!) {
                        textFiled.text = ""

                        currentTimer.schedule(
                            object : TimerTask() {
                                override fun run() {
                                    SwingUtilities.invokeLater { actionExecuteStatusPanel.isVisible = false }
                                }
                            }, 60000
                        )
                    }
                }
            }
        }
    }

    fun setCurrentAction(action: Action) {
        currentAction = action
        SwingUtilities.invokeLater {
            textFiled.text = ""
            actionExecuteStatusPanel.isVisible = false
        }
    }
}
