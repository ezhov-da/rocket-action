package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer

/**
 * Regression cover for the text, sort and condition rules of the Chains editor.
 */
class ChainActionsFilterTest {
    private val searchTextTransformer = SearchTextTransformer()

    private val first = chain(id = "1-first", name = "First chain", description = "Runs first")
    private val second = chain(
        id = "2-second",
        name = "Second chain",
        description = "Runs second",
        actions = 2,
        isTool = true,
    )
    private val third = chain(
        id = "3-third",
        name = "Third chain",
        description = "Runs third",
        actions = 3,
    )

    private val all = listOf(first, second, third)

    @Test
    fun `text search matches id, name and description`() {
        assertThat(search("1-first")).containsExactly(first)
        assertThat(search("second chain")).containsExactly(second)
        assertThat(search("runs third")).containsExactly(third)
        assertThat(search("chain")).containsExactlyElementsOf(all)
    }

    @Test
    fun `text search supports keyboard layout transformation`() {
        val transformed = searchTextTransformer.transformedText("руддщ")

        assertThat(transformed).containsExactly("руддщ", "hello")
        assertThat(
            ChainActionsFilter.filter(
                sortInfo = SortInfo(Direction.ASC, SortField.NAME),
                searchAction = SearchAction.SearchInfo(conditions = emptyList(), text = transformed),
                chains = all + chain(id = "4-hello", name = "hello chain", description = ""),
            ).map { it.id }
        ).containsExactly("4-hello")
    }

    @Test
    fun `empty search and reset keep every chain`() {
        assertThat(search("")).containsExactlyElementsOf(all)
        assertThat(
            ChainActionsFilter.filter(
                sortInfo = SortInfo(Direction.ASC, SortField.NAME),
                searchAction = SearchAction.Reset(emptyList()),
                chains = all,
            )
        ).containsExactlyElementsOf(all)
    }

    @Test
    fun `sorts by name and actions count`() {
        assertThat(filter(SortInfo(Direction.ASC, SortField.NAME)).map { it.id })
            .containsExactly("1-first", "2-second", "3-third")
        assertThat(filter(SortInfo(Direction.DESC, SortField.NAME)).map { it.id })
            .containsExactly("3-third", "2-second", "1-first")
        assertThat(filter(SortInfo(Direction.ASC, SortField.ACTIONS_COUNT)).map { it.actions.size })
            .containsExactly(1, 2, 3)
        assertThat(filter(SortInfo(Direction.DESC, SortField.ACTIONS_COUNT)).map { it.actions.size })
            .containsExactly(3, 2, 1)
    }

    @Test
    fun `condition filters select MCP and scheduled chains`() {
        assertThat(conditions(SearchAction.SearchCondition.MCP)).containsExactly(second)
        assertThat(
            ChainActionsFilter.filter(
                sortInfo = SortInfo(Direction.ASC, SortField.NAME),
                searchAction = SearchAction.SearchInfo(
                    conditions = listOf(SearchAction.SearchCondition.SCHEDULER),
                    text = emptyList(),
                ),
                chains = all,
                scheduledActionIds = setOf(first.id, third.id),
            ).map { it.id }
        ).containsExactly("1-first", "3-third")
    }

    @Test
    fun `text search is combined with condition filters`() {
        val result = ChainActionsFilter.filter(
            sortInfo = SortInfo(Direction.ASC, SortField.NAME),
            searchAction = SearchAction.SearchInfo(
                conditions = listOf(SearchAction.SearchCondition.MCP),
                text = searchTextTransformer.transformedText("chain"),
            ),
            chains = all,
        )

        assertThat(result).containsExactly(second)
    }

    private fun search(text: String): List<ChainAction> =
        ChainActionsFilter.filter(
            sortInfo = SortInfo(Direction.ASC, SortField.NAME),
            searchAction = SearchAction.SearchInfo(
                conditions = emptyList(),
                text = searchTextTransformer.transformedText(text),
            ),
            chains = all,
        )

    private fun filter(sortInfo: SortInfo): List<ChainAction> =
        ChainActionsFilter.filter(
            sortInfo = sortInfo,
            searchAction = SearchAction.SearchInfo(conditions = emptyList(), text = emptyList()),
            chains = all,
        )

    private fun conditions(condition: SearchAction.SearchCondition): List<ChainAction> =
        ChainActionsFilter.filter(
            sortInfo = SortInfo(Direction.ASC, SortField.NAME),
            searchAction = SearchAction.SearchInfo(conditions = listOf(condition), text = emptyList()),
            chains = all,
        )

    private fun chain(
        id: String,
        name: String,
        description: String,
        actions: Int = 1,
        isTool: Boolean = false,
    ) = ChainAction(
        id = id,
        name = name,
        description = description,
        actions = (1..actions).map { ActionOrder("$id-order-$it", "atomic-$it") },
        isTool = isTool,
    )
}
