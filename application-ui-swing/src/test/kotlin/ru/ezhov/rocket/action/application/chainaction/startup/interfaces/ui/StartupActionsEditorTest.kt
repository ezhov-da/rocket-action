package ru.ezhov.rocket.action.application.chainaction.startup.interfaces.ui

import io.mockk.every
import io.mockk.mockk
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
import ru.ezhov.rocket.action.application.chainaction.startup.application.StartupActionService
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntryResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunState
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunStatus
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger
import java.time.LocalDateTime

internal class StartupActionsEditorTest {
    private val inAction = atomic("in", ContractType.IN_OUT)
    private val unitAction = atomic("unit", ContractType.UNIT_UNIT)
    private val chain = ChainAction(
        id = "chain",
        name = "Chain",
        description = "",
        actions = listOf(ActionOrder(chainOrderId = "o1", actionId = "in")),
    )

    private val repository = InMemoryStartupActionsRepository()
    private var runState = StartupRunState.idle()

    private val service = StartupActionService(
        startupActionsRepository = repository,
        chainActionService = mockk<ChainActionService> {
            every { chains() } returns listOf(chain)
            every { byId(any()) } returns null
            every { byId("chain") } returns chain
        },
        atomicActionService = mockk<AtomicActionService> {
            every { atomics() } returns listOf(inAction, unitAction)
            every { atomicBy(any()) } returns null
            every { atomicBy("in") } returns inAction
            every { atomicBy("unit") } returns unitAction
        },
    )

    private fun editor() = StartupActionsEditor(service) { runState }

    @Test
    fun `should save the draft and reload it`() {
        val editor = editor()
        editor.addAction("in")
        editor.setInput("value")
        assertThat(editor.dirty).isTrue

        editor.save()

        assertThat(editor.dirty).isFalse
        val reloaded = editor()
        reloaded.reload()
        assertThat(reloaded.entries).hasSize(1)
        assertThat(reloaded.entries.single().actionId).isEqualTo("in")
        assertThat(reloaded.entries.single().input).isEqualTo("value")
    }

    @Test
    fun `should detect input acceptance for both action types`() {
        val editor = editor()
        editor.addAction("in")
        val inEntry = editor.entries.single()
        assertThat(editor.acceptsInput(inEntry)).isTrue

        editor.addAction("unit")
        val unitEntry = editor.entries.last()
        assertThat(editor.acceptsInput(unitEntry)).isFalse

        editor.addAction("chain")
        val chainEntry = editor.entries.last()
        assertThat(editor.acceptsInput(chainEntry)).isTrue
    }

    @Test
    fun `should track unsaved changes`() {
        val editor = editor()
        assertThat(editor.dirty).isFalse

        editor.addAction("in")
        assertThat(editor.dirty).isTrue

        editor.save()
        assertThat(editor.dirty).isFalse

        editor.setStopOnError(true)
        assertThat(editor.dirty).isTrue
        assertThat(editor.stopOnError).isTrue
    }

    @Test
    fun `should not allow editing while a run is active`() {
        val editor = editor()
        editor.addAction("in")
        editor.save()

        editor.applyRunState(StartupRunState(running = true, current = 1, total = 1))

        assertThat(editor.editable).isFalse
        editor.addAction("unit")
        editor.removeSelected()
        editor.setStopOnError(true)
        assertThat(editor.entries).hasSize(1)
        assertThat(editor.stopOnError).isFalse

        editor.applyRunState(finishedState(StartupRunStatus.SUCCESS))

        assertThat(editor.editable).isTrue
    }

    @Test
    fun `should re-enable editing after an unexpected failure`() {
        val editor = editor()
        editor.applyRunState(StartupRunState(running = true, current = 1, total = 1))
        assertThat(editor.editable).isFalse

        editor.applyRunState(finishedState(StartupRunStatus.ERROR))

        assertThat(editor.editable).isTrue
    }

    @Test
    fun `should allow run now only for a non-empty saved list when idle`() {
        val editor = editor()
        assertThat(editor.canRunNow).isFalse

        editor.addAction("in")
        assertThat(editor.canRunNow).isFalse

        editor.save()
        assertThat(editor.canRunNow).isTrue

        editor.applyRunState(StartupRunState(running = true, current = 0, total = 1))
        assertThat(editor.canRunNow).isFalse
    }

    @Test
    fun `should reorder entries`() {
        val editor = editor()
        editor.addAction("in")
        editor.addAction("unit")
        editor.select(1)
        editor.moveUp()

        assertThat(editor.entries.map { it.actionId }).containsExactly("unit", "in")
        assertThat(editor.selection).isEqualTo(0)
    }

    private fun finishedState(status: StartupRunStatus): StartupRunState {
        val entry = StartupActionEntry(id = "entry", actionId = "in")
        val result = StartupRunResult(
            trigger = StartupRunTrigger.MANUAL,
            startedAt = LocalDateTime.now(),
            finishedAt = LocalDateTime.now(),
            entries = listOf(StartupActionEntryResult(entry = entry, status = status)),
        )
        return StartupRunState.idle(result)
    }

    private fun atomic(id: String, contract: ContractType) = AtomicAction(
        id = id,
        name = id,
        description = "",
        contractType = contract,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = "",
    )

    private class InMemoryStartupActionsRepository : StartupActionsRepository {
        var value: StartupActions = StartupActions()

        override fun all(): StartupActions = value

        override fun save(startupActions: StartupActions) {
            value = startupActions
        }
    }
}
