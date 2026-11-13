package ru.ezhov.rocket.action.application.chainaction.scheduler.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.ActionSchedulerLogRepository
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.ActionSchedulerRepository
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus

class ActionSchedulerServiceTest {
    private val chainActionService = mockk<ChainActionService>(relaxed = true)
    private val atomicActionService = mockk<AtomicActionService>(relaxed = true)
    private val repository = mockk<ActionSchedulerRepository>(relaxed = true)
    private val logRepository = mockk<ActionSchedulerLogRepository>(relaxed = true)

    private val service = ActionSchedulerService(
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
        actionSchedulerRepository = repository,
        actionSchedulerLogRepository = logRepository,
    )

    @Test
    fun `a non-scheduled action does not load chains or atomic actions`() {
        every { repository.byActionId("action") } returns null

        assertThat(service.scheduler("action")).isNull()

        verify(exactly = 0) { chainActionService.byId(any()) }
        verify(exactly = 0) { chainActionService.chains() }
        verify(exactly = 0) { atomicActionService.atomicBy(any()) }
        verify(exactly = 0) { atomicActionService.atomics() }
    }

    @Test
    fun `a scheduled action returns the action, scheduler and log file`() {
        val action = atomicAction("action")
        val scheduler = ActionScheduler(
            actionId = action.id,
            cron = "0 0 * * * ?",
            dateOfTheLastLaunch = null,
            schedulerStatus = ActionSchedulerStatus.SUCCESS,
        )
        every { repository.byActionId(action.id) } returns scheduler
        every { chainActionService.byId(action.id) } returns null
        every { atomicActionService.atomicBy(action.id) } returns action
        every { logRepository.get(action.id) } returns null

        val dto = service.scheduler(action.id)

        assertThat(dto?.action).isEqualTo(action)
        assertThat(dto?.actionScheduler).isEqualTo(scheduler)
        assertThat(dto?.logFile).isNull()
        verify(exactly = 0) { chainActionService.chains() }
        verify(exactly = 0) { atomicActionService.atomics() }
    }

    private fun atomicAction(id: String) =
        AtomicAction(
            id = id,
            name = id,
            description = id,
            contractType = ContractType.UNIT_UNIT,
            engine = AtomicActionEngine.GROOVY,
            source = AtomicActionSource.TEXT,
            data = id,
        )
}
