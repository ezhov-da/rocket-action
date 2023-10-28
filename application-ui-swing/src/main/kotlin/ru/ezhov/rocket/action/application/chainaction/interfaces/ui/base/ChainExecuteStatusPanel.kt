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

private val logger = KotlinLogging.logger {}

class ChainExecuteStatusPanel(
    private val chainActionExecutorService: ChainActionExecutorService
) : JPanel(MigLayout(/*"debug"*/)) {

    fun executeChain(input: String, chainAction: ChainAction, onComplete: () -> Unit) {
        removeAll()
        val map = chainAction.actionIds.associateWith { JLabel(" ").apply { isOpaque = true } }
        map.values.forEach { add(it, "width max") }

        repaint()
        revalidate()

        Thread {
            chainActionExecutorService.execute(
                input,
                chainAction.id,
                object : ChainActionExecutorProgress {
                    override fun complete(result: Any?) {
                        // TODO ezhov
                        println(result)
                        onComplete()
                    }

                    override fun success(atomicAction: AtomicAction) {
                        map[atomicAction.id]!!.background = Color.GREEN
                    }

                    override fun failure(id: String, atomicAction: AtomicAction?, ex: Exception) {
                        map[id]!!.background = Color.RED

                        logger.warn(ex) { "Error on action ID '$id'. Action '$atomicAction'" }
                    }
                }
            )
        }.start()
    }
}
