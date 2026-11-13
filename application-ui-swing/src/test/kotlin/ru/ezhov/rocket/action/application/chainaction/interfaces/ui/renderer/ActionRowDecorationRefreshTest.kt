package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus

/**
 * The editor panels rebuild the snapshot from the collections they already hold, so a domain
 * event must not touch a repository or a service.
 */
class ActionRowDecorationRefreshTest {
    private val atomicActionService = mockk<AtomicActionService>(relaxed = true)
    private val chainActionService = mockk<ChainActionService>(relaxed = true)
    private val actionSchedulerService = mockk<ActionSchedulerService>(relaxed = true)

    private val action = atomic(id = "action", name = "Before rename")
    private val chain = ChainAction(
        id = "chain",
        name = "Chain",
        description = "",
        actions = listOf(ActionOrder("order", action.id)),
    )

    @Test
    fun `an action update event changes the rendered name without reading repositories`() {
        val renamed = action.copy(name = "After rename")

        val decoration = ActionRowDecorationBuilder.build(
            chains = listOf(chain),
            atomics = listOf(renamed),
            schedulers = emptyList(),
        )[action.id]!!

        assertThat(decoration.titleHtml).contains("After rename")
        verifyNoRepositoryReads()
    }

    @Test
    fun `a scheduler event changes the decoration without reading repositories`() {
        val decoration = ActionRowDecorationBuilder.build(
            chains = listOf(chain),
            atomics = listOf(action),
            schedulers = listOf(
                ActionScheduler(
                    actionId = action.id,
                    cron = null,
                    dateOfTheLastLaunch = null,
                    schedulerStatus = ActionSchedulerStatus.ERROR,
                )
            ),
        )[action.id]!!

        assertThat(decoration.schedulerStatus).isEqualTo(ActionSchedulerStatus.ERROR)
        verifyNoRepositoryReads()
    }

    private fun verifyNoRepositoryReads() {
        verify(exactly = 0) { atomicActionService.atomics() }
        verify(exactly = 0) { atomicActionService.atomicBy(any()) }
        verify(exactly = 0) { chainActionService.chains() }
        verify(exactly = 0) { actionSchedulerService.schedulers() }
        verify(exactly = 0) { actionSchedulerService.scheduler(any()) }
    }

    private fun atomic(id: String, name: String) = AtomicAction(
        id = id,
        name = name,
        description = name,
        contractType = ContractType.IN_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = name,
    )
}
