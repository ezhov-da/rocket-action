package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import mu.KotlinLogging
import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutorProgress
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

private val logger = KotlinLogging.logger {}

class ChainExecuteStatusPanel(
    private val chainActionExecutorService: ChainActionExecutorService
) : JPanel(MigLayout(/*"debug"*/"insets 0 0 0 0")) {

    fun executeChain(input: String?, chainAction: ChainAction, onComplete: () -> Unit) {
        removeAll()
        val map = chainAction
            .actions
            .associate { it.chainOrderId to ProgressActionInfo(actionOrder = it) }

        map.values.forEach { add(it.label, "width max") }

        repaint()
        revalidate()

        val chainExecuteStatusPanel = this

        Thread {
            chainActionExecutorService.execute(
                input,
                chainAction.id,
                object : ChainActionExecutorProgress {
                    override fun onChainComplete(result: Any?, lastAtomicAction: AtomicAction) {
                        // TODO ezhov
                        onComplete()

                        if (
                            lastAtomicAction.contractType == ContractType.IN_OUT ||
                            lastAtomicAction.contractType == ContractType.UNIT_OUT
                        ) {
                            result?.let {
                                ResultChainDialog(result.toString())
                                    .apply {
                                        setLocationRelativeTo(chainExecuteStatusPanel)
                                        isVisible = true
                                    }
                            }
                        }
                    }

                    override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                        SwingUtilities.invokeLater {
                            map[orderId]!!.setSuccess(ProgressActionInfo.Success(result, atomicAction))
                        }
                    }

                    override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                        SwingUtilities.invokeLater {
                            map[orderId]!!.setFailure(ProgressActionInfo.Failure(atomicAction, ex))
                        }
                        logger.warn(ex) {
                            "Error on action ID '${atomicAction?.id} in orderId '$orderId''. Action '$atomicAction'"
                        }
                    }
                }
            )
        }.start()
    }

    private data class ProgressActionInfo(
        val actionOrder: ActionOrder,
        private val resultChainPanel: ResultChainPanel = ResultChainPanel(null),
        private val popupMenu: JPopupMenu = JPopupMenu().apply {
            add(resultChainPanel)
            setPopupSize(400, 300)
        },
        private var success: Success? = null,
        private var failure: Failure? = null,
        val label: JLabel = JLabel(" ")
            .apply {
                background = Color.GRAY
                isOpaque = true
            },
    ) {
        init {
            label.addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(e: MouseEvent) {
                    val text = when {
                        success == null -> failure!!.exception.stackTraceToString()
                        failure == null -> success!!.result?.toString().orEmpty()
                        else -> "No execution result"
                    }

                    resultChainPanel.setText(text)

                    popupMenu.show(label, e.x, e.y)
                }
            })
        }

        fun setSuccess(result: Success) {
            this.success = result

            label.background = Color.GREEN
        }

        fun setFailure(result: Failure) {
            this.failure = result

            label.background = Color.RED
        }

        data class Success(
            val result: Any?,
            val atomicAction: AtomicAction
        )

        data class Failure(
            val atomicAction: AtomicAction?,
            val exception: Exception,
        )
    }
}
