package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType

class AtomicActionsFilterTest {
    private val sortInfo = SortInfo(direction = Direction.ASC, sortField = SortField.NAME)

    private val mcpAction = action(id = "mcp", name = "MCP", isTool = true)
    private val scheduledAction = action(id = "scheduled", name = "Scheduled")
    private val plainAction = action(id = "plain", name = "Plain")

    private val actions = listOf(mcpAction, scheduledAction, plainAction)

    @Test
    fun `filters actions used for MCP`() {
        val filtered = AtomicActionsFilter.filter(
            sortInfo = sortInfo,
            searchAction = searchAction(SearchAction.SearchCondition.MCP),
            actions = actions,
        )

        assertThat(filtered).containsExactly(mcpAction)
    }

    @Test
    fun `filters actions used in scheduler`() {
        val filtered = AtomicActionsFilter.filter(
            sortInfo = sortInfo,
            searchAction = searchAction(SearchAction.SearchCondition.SCHEDULER),
            actions = actions,
            scheduledActionIds = setOf(scheduledAction.id),
        )

        assertThat(filtered).containsExactly(scheduledAction)
    }

    @Test
    fun `combines MCP and scheduler conditions`() {
        val filtered = AtomicActionsFilter.filter(
            sortInfo = sortInfo,
            searchAction = searchAction(
                SearchAction.SearchCondition.MCP,
                SearchAction.SearchCondition.SCHEDULER,
            ),
            actions = actions,
            scheduledActionIds = setOf(mcpAction.id),
        )

        assertThat(filtered).containsExactly(mcpAction)
    }

    private fun searchAction(vararg conditions: SearchAction.SearchCondition) =
        SearchAction.SearchInfo(conditions = conditions.toList(), text = emptyList())

    private fun action(id: String, name: String, isTool: Boolean = false) =
        AtomicAction(
            id = id,
            name = name,
            description = name,
            contractType = ContractType.IN_OUT,
            engine = AtomicActionEngine.GROOVY,
            source = AtomicActionSource.TEXT,
            data = name,
            isTool = isTool,
        )
}
