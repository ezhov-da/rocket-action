package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.AtomicActionsFilter
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.Direction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.SearchAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.SortField
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.SortInfo
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecorationBuilder
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import java.awt.Component
import java.awt.Dimension
import java.awt.image.BufferedImage
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import kotlin.system.measureTimeMillis

/**
 * Smoke test for the scenario that used to freeze the UI: a large profile with big script bodies.
 */
class ActionListPerformanceTest {
    private val atomics = (0 until 1000).map { atomic("action-$it") }
    private val schedulers = (0 until 100).map { index ->
        ActionScheduler(
            actionId = "action-$index",
            cron = "0 0 * * * ?",
            dateOfTheLastLaunch = null,
            schedulerStatus = ActionSchedulerStatus.SUCCESS,
        )
    }
    private val chains = listOf(
        ChainAction(
            id = "chain",
            name = "Chain",
            description = "",
            actions = atomics.take(200).mapIndexed { index, action ->
                ActionOrder("order-$index", action.id)
            },
        )
    )

    @Test
    @Timeout(30)
    fun `search, reset and a layout pass over 1000 actions stay bounded`() {
        val duration = measureTimeMillis {
            val found = AtomicActionsFilter.filter(
                sortInfo = SortInfo(Direction.ASC, SortField.NAME),
                searchAction = SearchAction.SearchInfo(
                    conditions = emptyList(),
                    text = listOf("action-999"),
                ),
                actions = atomics,
                scheduledActionIds = schedulers.map { it.actionId }.toSet(),
            )
            assertThat(found.map { it.id }).containsExactly("action-999")

            val reset = AtomicActionsFilter.filter(
                sortInfo = SortInfo(Direction.ASC, SortField.NAME),
                searchAction = SearchAction.Reset(emptyList()),
                actions = atomics,
                scheduledActionIds = schedulers.map { it.actionId }.toSet(),
            )
            assertThat(reset).hasSize(1000)

            val snapshot = ActionRowDecorationBuilder.build(
                chains = chains,
                atomics = atomics,
                schedulers = schedulers,
            )
            assertThat(snapshot["action-0"]?.schedulerStatus).isEqualTo(ActionSchedulerStatus.SUCCESS)

            val renderer = CountingRenderer()
            val list = JList<AtomicAction>()
            list.cellRenderer = renderer
            list.configureFixedRowHeight(24)
            val model = DefaultListModel<AtomicAction>()
            atomics.forEach { model.addElement(it) }
            list.model = model
            list.size = Dimension(400, 600)
            list.paint(BufferedImage(400, 600, BufferedImage.TYPE_INT_ARGB).createGraphics())

            assertThat(renderer.calls).isBetween(1, 60)
        }

        assertThat(duration).isLessThan(5_000)
    }

    private fun atomic(id: String) = AtomicAction(
        id = id,
        name = "Action $id",
        description = "Description of $id",
        contractType = ContractType.IN_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = "script body ".repeat(200),
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
