package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import mu.KotlinLogging
import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import java.util.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

private val logger = KotlinLogging.logger {}

class ActionExecutePanel(
    actionExecutorService: ActionExecutorService
) : JPanel(MigLayout(/*"debug"*/"insets 0 0 0 0")) {

    private val infoLabel = JLabel("<html>Testing. <b>Save action before test</b>")
    private val inputTextPane: RSyntaxTextArea = RSyntaxTextArea()
    private val doTestButton = JButton("Do test")
    private val actionExecuteStatusPanel = ActionExecuteStatusPanel(actionExecutorService).apply { isVisible = false }

    private val currentTimer: Timer = Timer()

    private var currentAction: Action? = null

    init {
        add(infoLabel, "wrap")
        add(RTextScrollPane(inputTextPane, false), "span, grow, width max, height max")
        add(doTestButton, "wrap")
        add(actionExecuteStatusPanel, "hmax 6, width max, hidemode 2")

        doTestButton.addActionListener {
            when (currentAction) {
                null -> {
                    logger.warn { "Action not set for test" }
                }

                else -> {
                    actionExecuteStatusPanel.isVisible = true

                    actionExecuteStatusPanel.executeChain(input = inputTextPane.text, action = currentAction!!) {
                        inputTextPane.text = ""

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
            inputTextPane.text = ""
            actionExecuteStatusPanel.isVisible = false
        }
    }
}
