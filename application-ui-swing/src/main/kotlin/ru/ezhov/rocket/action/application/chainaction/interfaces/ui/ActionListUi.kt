package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import java.awt.Font
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.SwingWorker
import javax.swing.UIManager
import java.util.concurrent.CancellationException
import kotlin.math.max

/**
 * Row height shared by every action list.
 *
 * Setting a fixed height makes `BasicListUI.updateLayoutState()` skip measuring the whole
 * model, which used to render every row (and read every repository file) on each refill.
 */
object ActionListCellHeight {
    fun atomic(): Int = measure(
        AtomicActionListCellPanel(
            atomicAction = AtomicAction(
                id = "prototype",
                name = "Prototype action",
                description = "",
                contractType = ContractType.IN_OUT,
                engine = AtomicActionEngine.GROOVY,
                source = AtomicActionSource.TEXT,
                data = "",
            )
        )
    )

    fun chain(): Int = measure(
        ChainActionListCellPanel(
            chainAction = ChainAction(
                id = "prototype",
                name = "Prototype chain",
                description = "",
                actions = emptyList(),
            )
        )
    )

    private fun measure(prototype: JComponent): Int {
        prototype.font = listFont()
        val measured = prototype.preferredSize.height
        val textHeight = prototype.getFontMetrics(prototype.font).height + 8

        return max(measured, textHeight) + 2
    }

    private fun listFont(): Font =
        UIManager.getFont("List.font")
            ?: UIManager.getFont("Label.font")
            ?: Font("Dialog", Font.PLAIN, 12)
}

/**
 * Sets a fixed row height. The JDK only skips the full-model measurement when **both**
 * `fixedCellWidth` and `fixedCellHeight` are set, so the width follows the list width.
 */
fun JList<*>.configureFixedRowHeight(height: Int) {
    fixedCellHeight = height
    fixedCellWidth = 1
    addComponentListener(object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent) {
            val width = width
            if (width > 0 && fixedCellWidth != width) {
                fixedCellWidth = width
            }
        }
    })
}

/**
 * Replaces the model with a detached one, so one refill produces a single model change event.
 * [DefaultListModel] is kept because the chain editor drag-and-drop handler requires it.
 */
fun <T> JList<T>.replaceItems(items: List<T>): DefaultListModel<T> {
    val model = DefaultListModel<T>()
    items.forEach { model.addElement(it) }
    this.model = model
    return model
}

/**
 * Runs search work in the background and applies only the latest submitted result.
 *
 * Starting a new search cancels the previous worker and invalidates its result.
 */
class LatestSearchWorker<T>(
    private val onResult: (T) -> Unit,
    private val onError: (Throwable) -> Unit = {},
) {
    private var generation = 0L
    private var current: SwingWorker<T, Unit>? = null

    fun submit(task: () -> T) {
        val token: Long
        synchronized(this) {
            current?.cancel(true)
            token = ++generation
            val worker = object : SwingWorker<T, Unit>() {
                override fun doInBackground(): T = task()

                override fun done() {
                    if (isCancelled || !isCurrent(token)) {
                        return
                    }

                    try {
                        onResult(get())
                    } catch (ex: CancellationException) {
                        // Superseded by a newer search.
                    } catch (ex: Exception) {
                        onError(ex)
                    }
                }
            }
            current = worker
            worker.execute()
        }
    }

    fun cancel() {
        synchronized(this) {
            current?.cancel(true)
            generation++
        }
    }

    @Synchronized
    private fun isCurrent(token: Long): Boolean = generation == token
}
