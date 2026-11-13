package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import java.util.concurrent.TimeUnit

class SelectChainListPanelTest {
    private val chains = listOf(
        chain("chain-1", "Chain one"),
        chain("chain-2", "Chain two"),
    )
    private val atomics = listOf(
        atomic("atomic-1", "Atomic one"),
        atomic("atomic-2", "Atomic two"),
    )

    private val panel = SelectChainListPanel(
        chains = chains,
        atomics = atomics,
        searchTextTransformer = SearchTextTransformer(),
        selectedChainCallback = {},
    )

    @Test
    @Timeout(10)
    fun `the list is filled with chains and atomic actions`() {
        waitUntil { panel.currentItems().size == 4 }

        assertThat(panel.currentItems().map { it.id() })
            .containsExactlyInAnyOrder("chain-1", "chain-2", "atomic-1", "atomic-2")
    }

    @Test
    @Timeout(10)
    fun `clearing the search restores every eligible action`() {
        waitUntil { panel.currentItems().size == 4 }

        panel.search("Chain")
        waitUntil { panel.currentItems().map { it.id() }.toSet() == setOf("chain-1", "chain-2") }

        panel.clearSearch()

        waitUntil { panel.currentItems().size == 4 }
        assertThat(panel.currentItems().map { it.id() })
            .containsExactlyInAnyOrder("chain-1", "chain-2", "atomic-1", "atomic-2")
    }

    @Test
    @Timeout(10)
    fun `searching for an atomic action keeps chains out of the result`() {
        waitUntil { panel.currentItems().size == 4 }

        panel.search("Atomic two")

        waitUntil { panel.currentItems().size == 1 }
        assertThat(panel.currentItems().map { it.id() }).containsExactly("atomic-2")
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

    private fun chain(id: String, name: String) = ChainAction(
        id = id,
        name = name,
        description = name,
        actions = listOf(ActionOrder("$id-order", "atomic-1")),
    )

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
