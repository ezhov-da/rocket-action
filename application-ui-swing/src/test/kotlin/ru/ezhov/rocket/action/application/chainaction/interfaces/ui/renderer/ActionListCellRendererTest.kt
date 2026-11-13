package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.AtomicActionListCellPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainActionListCellPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.SelectedAtomicAction
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import java.awt.Component
import java.awt.Container
import javax.swing.JLabel
import javax.swing.JList

/**
 * Every renderer is built without services, so rendering cannot touch a repository or a service.
 */
class ActionListCellRendererTest {
    private val atomic = AtomicAction(
        id = "atomic",
        name = "Atomic name",
        description = "Atomic description",
        contractType = ContractType.IN_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = "data",
    )
    private val chain = ChainAction(
        id = "chain",
        name = "Chain name",
        description = "Chain description",
        actions = listOf(ActionOrder("order", atomic.id)),
    )
    private val snapshot = ActionRowDecorationBuilder.build(
        chains = listOf(chain),
        atomics = listOf(atomic),
        schedulers = listOf(
            ActionScheduler(
                actionId = atomic.id,
                cron = null,
                dateOfTheLastLaunch = null,
                schedulerStatus = ActionSchedulerStatus.SUCCESS,
            )
        ),
    )

    @Test
    fun `renders an atomic action from the snapshot`() {
        val cell = AtomicActionListCellRenderer(snapshot)
            .getListCellRendererComponent(JList<Any>(), atomic, 0, false, false)

        assertThat(cell).isInstanceOf(AtomicActionListCellPanel::class.java)
        assertThat(texts(cell)).anySatisfy { assertThat(it).contains("Atomic name").contains("<strong>1</strong>") }
    }

    @Test
    fun `renders a chain action from the snapshot`() {
        val cell = ChainActionListCellRenderer(snapshot)
            .getListCellRendererComponent(JList<Any>(), chain, 0, false, false)

        assertThat(cell).isInstanceOf(ChainActionListCellPanel::class.java)
        assertThat(texts(cell)).contains("Chain name")
    }

    @Test
    fun `renders a selected atomic action from the snapshot`() {
        val cell = OrderAtomicActionListCellRenderer(snapshot)
            .getListCellRendererComponent(
                JList<Any>(),
                SelectedAtomicAction(atomicAction = atomic),
                0,
                false,
                false,
            )

        assertThat(cell).isInstanceOf(AtomicActionListCellPanel::class.java)
        assertThat(texts(cell)).anySatisfy { assertThat(it).contains("Atomic name") }
    }

    @Test
    fun `renders chain and atomic actions from the snapshot`() {
        val renderer = ChainAndAtomicActionListCellRenderer(snapshot)

        val chainCell = renderer.getListCellRendererComponent(JList<Any>(), chain, 0, false, false)
        val atomicCell = renderer.getListCellRendererComponent(JList<Any>(), atomic, 0, false, false)

        assertThat(chainCell).isInstanceOf(ChainActionListCellPanel::class.java)
        assertThat(atomicCell).isInstanceOf(AtomicActionListCellPanel::class.java)
    }

    private fun texts(component: Component): List<String> =
        buildList {
            if (component is JLabel) {
                component.text?.let { add(it) }
            }
            if (component is Container) {
                component.components.forEach { addAll(texts(it)) }
            }
        }
}
