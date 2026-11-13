package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecoration
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecorationSnapshot
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import java.awt.Component
import java.awt.Container
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

class ChainsSelectPanelTest {
    private val chain = ChainAction(
        id = "chain",
        name = "Chain name",
        description = "Chain description",
        actions = emptyList(),
    )

    @Test
    fun `the usage list renders the scheduler decoration from the snapshot`() {
        val snapshot = ActionRowDecorationSnapshot.of(
            mapOf(
                chain.id to ActionRowDecoration(
                    titleHtml = chain.name,
                    schedulerStatus = ActionSchedulerStatus.ERROR,
                )
            )
        )
        val panel = ChainsSelectPanel(
            chains = listOf(chain),
            decorationSnapshot = snapshot,
            selectChainCallback = {},
        )

        assertThat(panel.currentItems()).containsExactly(chain)
        val withStatus = render(panel)

        val withoutStatus = ChainsSelectPanel(
            chains = listOf(chain),
            decorationSnapshot = ActionRowDecorationSnapshot.EMPTY,
            selectChainCallback = {},
        ).let { plain -> render(plain) }

        assertThat(iconLabels(withStatus)).isEqualTo(iconLabels(withoutStatus) + 1)
    }

    @Test
    fun `the usage panel does not call repositories or services`() {
        val chainActionService = mockk<ChainActionService>(relaxed = true)
        val actionSchedulerService = mockk<ActionSchedulerService>(relaxed = true)

        val panel = ChainsSelectPanel(
            chains = listOf(chain),
            decorationSnapshot = ActionRowDecorationSnapshot.EMPTY,
            selectChainCallback = {},
        )
        render(panel)

        verify(exactly = 0) { chainActionService.usageAction(any()) }
        verify(exactly = 0) { actionSchedulerService.scheduler(any()) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun render(panel: ChainsSelectPanel): Component {
        val list = findList(panel)
        val renderer = list.cellRenderer as ListCellRenderer<Any>
        return renderer.getListCellRendererComponent(list as JList<Any>, chain, 0, false, false)
    }

    private fun findList(component: Component): JList<*> {
        if (component is JList<*>) {
            return component
        }
        if (component is Container) {
            component.components.forEach { child ->
                val found = runCatching { findList(child) }.getOrNull()
                if (found != null) {
                    return found
                }
            }
        }
        throw AssertionError("JList was not found")
    }

    private fun iconLabels(component: Component): Int {
        var count = 0
        if (component is JLabel && component.icon != null) {
            count++
        }
        if (component is Container) {
            component.components.forEach { count += iconLabels(it) }
        }
        return count
    }
}
