package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import java.awt.Component
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.SwingUtilities

class ActionListUiTest {
    @Test
    fun `a list with a fixed row height does not render the whole model`() {
        val renderer = CountingRenderer()
        val list = JList<AtomicAction>()
        list.cellRenderer = renderer
        list.configureFixedRowHeight(24)

        val model = DefaultListModel<AtomicAction>()
        repeat(1000) { model.addElement(action("$it")) }
        list.model = model

        list.preferredSize

        assertThat(renderer.calls).isZero()
    }

    @Test
    fun `only the latest search result is applied`() {
        val firstStarted = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val secondApplied = CountDownLatch(1)
        val results = CopyOnWriteArrayList<String>()

        val worker = LatestSearchWorker<String>(onResult = { result ->
            results += result
            secondApplied.countDown()
        })

        worker.submit {
            firstStarted.countDown()
            releaseFirst.await(5, TimeUnit.SECONDS)
            "first"
        }

        assertThat(firstStarted.await(5, TimeUnit.SECONDS)).isTrue()

        worker.submit { "second" }

        assertThat(secondApplied.await(5, TimeUnit.SECONDS)).isTrue()
        releaseFirst.countDown()
        Thread.sleep(300)

        assertThat(results).containsExactly("second")
    }

    @Test
    fun `a refill fires a single model change event`() {
        val list = JList<Int>()
        var changes = 0
        list.addPropertyChangeListener("model") { changes++ }

        list.replaceItems(listOf(1, 2, 3))

        assertThat(changes).isEqualTo(1)
        assertThat(list.model.size).isEqualTo(3)
    }

    @Test
    fun `repository access happens off the event thread and a stale refresh is ignored`() {
        val atomicActionService = mockk<AtomicActionService>()
        val firstStarted = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val applied = CopyOnWriteArrayList<List<AtomicAction>>()
        val appliedOnEventThread = CopyOnWriteArrayList<Boolean>()

        every { atomicActionService.atomics() } answers {
            assertThat(SwingUtilities.isEventDispatchThread()).isFalse
            listOf(action("fresh"))
        }

        val worker = LatestSearchWorker<List<AtomicAction>>(onResult = { result ->
            applied += result
            appliedOnEventThread += SwingUtilities.isEventDispatchThread()
        })

        worker.submit {
            firstStarted.countDown()
            releaseFirst.await(5, TimeUnit.SECONDS)
            atomicActionService.atomics() + action("stale")
        }

        assertThat(firstStarted.await(5, TimeUnit.SECONDS)).isTrue()

        worker.submit { atomicActionService.atomics() }
        waitUntil { applied.isNotEmpty() }

        releaseFirst.countDown()
        Thread.sleep(300)

        assertThat(applied).hasSize(1)
        assertThat(applied.single().map { it.id }).containsExactly("fresh")
        assertThat(appliedOnEventThread).containsOnly(true)
    }

    private fun waitUntil(condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (System.nanoTime() < deadline) {
            if (condition()) {
                return
            }
            Thread.sleep(10)
        }
        throw AssertionError("Condition was not met in time")
    }

    private fun action(id: String) =
        AtomicAction(
            id = id,
            name = "Action $id",
            description = "Description",
            contractType = ContractType.IN_OUT,
            engine = AtomicActionEngine.GROOVY,
            source = AtomicActionSource.TEXT,
            data = "data",
        )

    private class CountingRenderer : DefaultListCellRenderer() {
        var calls = 0

        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            calls++
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
        }
    }
}
