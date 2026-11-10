package ru.ezhov.rocket.action.application.chainaction.startup.application

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory

internal class StartupActionServiceTest {
    private val inAtom = atomic("in", ContractType.IN_OUT)
    private val unitAtom = atomic("unit", ContractType.UNIT_UNIT)
    private val chainWithInputFirst = ChainAction(
        id = "chain-in",
        name = "Chain with input",
        description = "",
        actions = listOf(ActionOrder(chainOrderId = "o1", actionId = "in")),
    )
    private val chainWithUnitFirst = ChainAction(
        id = "chain-unit",
        name = "Chain without input",
        description = "",
        actions = listOf(ActionOrder(chainOrderId = "o1", actionId = "unit")),
    )
    private val emptyChain = ChainAction(
        id = "chain-empty",
        name = "Empty chain",
        description = "",
        actions = emptyList(),
    )

    private val repository = InMemoryStartupActionsRepository()

    private val atomicActionService = mockk<AtomicActionService> {
        every { atomics() } returns listOf(inAtom, unitAtom)
        every { atomicBy(any()) } returns null
        every { atomicBy("in") } returns inAtom
        every { atomicBy("unit") } returns unitAtom
    }

    private val chainActionService = mockk<ChainActionService> {
        every { chains() } returns listOf(chainWithInputFirst, chainWithUnitFirst, emptyChain)
        every { byId(any()) } returns null
        every { byId("chain-in") } returns chainWithInputFirst
        every { byId("chain-unit") } returns chainWithUnitFirst
        every { byId("chain-empty") } returns emptyChain
    }

    private val service = StartupActionService(repository, chainActionService, atomicActionService)

    @Test
    fun `should detect input acceptance of atomic actions`() {
        assertThat(service.acceptsInput("in")).isTrue
        assertThat(service.acceptsInput("unit")).isFalse
    }

    @Test
    fun `should detect input acceptance of chains by the first action`() {
        assertThat(service.acceptsInput("chain-in")).isTrue
        assertThat(service.acceptsInput("chain-unit")).isFalse
        assertThat(service.acceptsInput("chain-empty")).isFalse
        assertThat(service.acceptsInput("missing")).isFalse
    }

    @Test
    fun `should add and remove entries`() {
        val added = service.add(StartupActions(), "in")
        assertThat(added.startupActions).hasSize(1)
        assertThat(added.startupActions.single().actionId).isEqualTo("in")

        val removed = service.remove(added, added.startupActions.single().id)
        assertThat(removed.startupActions).isEmpty()
    }

    @Test
    fun `should reorder entries`() {
        var startupActions = service.add(service.add(StartupActions(), "in"), "unit")
        val firstId = startupActions.startupActions[0].id
        val secondId = startupActions.startupActions[1].id

        startupActions = service.moveDown(startupActions, firstId)
        assertThat(startupActions.startupActions.map { it.id }).containsExactly(secondId, firstId)

        startupActions = service.moveUp(startupActions, firstId)
        assertThat(startupActions.startupActions.map { it.id }).containsExactly(firstId, secondId)

        startupActions = service.moveUp(startupActions, firstId)
        assertThat(startupActions.startupActions.map { it.id }).containsExactly(firstId, secondId)
    }

    @Test
    fun `should update enabled flag and input`() {
        var startupActions = service.add(StartupActions(), "in")
        val entryId = startupActions.startupActions.single().id

        startupActions = service.setEnabled(startupActions, entryId, false)
        assertThat(startupActions.startupActions.single().enabled).isFalse

        startupActions = service.setInput(startupActions, entryId, "value")
        assertThat(startupActions.startupActions.single().input).isEqualTo("value")

        startupActions = service.setInput(startupActions, entryId, "")
        assertThat(startupActions.startupActions.single().input).isNull()
    }

    @Test
    fun `should toggle stop on error`() {
        val updated = service.setStopOnError(StartupActions(), true)

        assertThat(updated.stopOnError).isTrue
    }

    @Test
    fun `should remove entries when an atomic action is deleted`() {
        saveEntries("in", "unit")

        DomainEventFactory.publisher.publish(listOf(AtomicActionDeletedDomainEvent("in")))

        assertThat(repository.value.startupActions.map { it.actionId }).containsExactly("unit")
    }

    @Test
    fun `should remove entries when a chain action is deleted`() {
        saveEntries("chain-in", "chain-unit")

        DomainEventFactory.publisher.publish(listOf(ChainActionDeletedDomainEvent("chain-in")))

        assertThat(repository.value.startupActions.map { it.actionId }).containsExactly("chain-unit")
    }

    private fun saveEntries(vararg actionIds: String) {
        repository.value = StartupActions(
            startupActions = actionIds.map { StartupActionEntry(id = "entry-$it", actionId = it) }
        )
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
