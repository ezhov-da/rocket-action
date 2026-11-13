package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecoration
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import java.awt.Component
import java.awt.Container
import javax.swing.JLabel

class ActionListCellPanelRenderingTest {
    private val chainActionService = mockk<ChainActionService>()
    private val actionSchedulerService = mockk<ActionSchedulerService>()

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

    @Test
    fun `atomic cell renders the snapshot decoration without calling services`() {
        val panel = AtomicActionListCellPanel(
            atomicAction = atomic,
            decoration = ActionRowDecoration(
                titleHtml = "<html>Atomic name. <b>2</b></html>",
                schedulerStatus = ActionSchedulerStatus.SUCCESS,
                usageCount = 2,
            ),
        )

        assertThat(panel.componentCount).isPositive
        assertThat(panel.preferredSize.height).isPositive
        assertThat(texts(panel)).anySatisfy { assertThat(it).contains("Atomic name") }
        verify(exactly = 0) { chainActionService.usageAction(any()) }
        verify(exactly = 0) { actionSchedulerService.scheduler(any()) }
    }

    @Test
    fun `chain cell renders the snapshot decoration without calling services`() {
        val panel = ChainActionListCellPanel(
            chainAction = chain,
            decoration = ActionRowDecoration(
                titleHtml = "Chain name",
                schedulerStatus = ActionSchedulerStatus.ERROR,
            ),
        )

        assertThat(panel.componentCount).isPositive
        assertThat(texts(panel)).contains("Chain name")
        verify(exactly = 0) { actionSchedulerService.scheduler(any()) }
    }

    private fun texts(component: Component): List<String> =
        buildList {
            if (component is JLabel) {
                add(component.text.orEmpty())
            }
            if (component is Container) {
                component.components.forEach { addAll(texts(it)) }
            }
        }
}
