package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import mu.KotlinLogging
import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutorProgress
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

private val logger = KotlinLogging.logger {}

class ChainExecuteStatusPanel(
    private val chainActionExecutorService: ChainActionExecutorService
) : JPanel(MigLayout(/*"debug"*/)) {

    fun executeChain(input: String, chainAction: ChainAction, onComplete: () -> Unit) {
        removeAll()
        val map = chainAction
            .actions
            .associate { it.chainOrderId to (it to JLabel(" ").apply { isOpaque = true }) }
        map.values.forEach { add(it.second, "width max") }

        repaint()
        revalidate()

        val chainExecuteStatusPanel = this

        Thread {
            chainActionExecutorService.execute(
                input,
                chainAction.id,
                object : ChainActionExecutorProgress {
                    override fun complete(result: Any?) {
                        // TODO ezhov
                        onComplete()

                        result?.let {
                            ResultChainDialog(result.toString())
                                .apply {
                                    setLocationRelativeTo(chainExecuteStatusPanel)
                                    isVisible = true
                                }
                        }
                    }

                    override fun success(orderId: String, atomicAction: AtomicAction) {
                        SwingUtilities.invokeLater {
                            map[orderId]!!.second.background = Color.GREEN
                        }
                    }

                    override fun failure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                        SwingUtilities.invokeLater {
                            map[orderId]!!.second.background = Color.RED
                        }
                        logger.warn(ex) {
                            "Error on action ID '${atomicAction?.id} in orderId '$orderId''. Action '$atomicAction'"
                        }
                    }
                }
            )
        }.start()
    }
}