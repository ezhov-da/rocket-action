package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer

/**
 * Regression cover for the text, sort and condition rules of the Actions editor.
 */
class AtomicActionsFilterTest {
    private val searchTextTransformer = SearchTextTransformer()

    private val groovyInOut = atomic(
        id = "1-groovy",
        name = "Groovy action",
        description = "Writes a groovy script",
        contractType = ContractType.IN_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = "println 'hello from data'",
    )
    private val fileUnitUnitWithAlias = atomic(
        id = "2-file",
        name = "File action",
        description = "Reads a file",
        contractType = ContractType.UNIT_UNIT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.FILE,
        data = "/tmp/file.txt",
        alias = "hello-alias",
    )
    private val mcpUnitOut = atomic(
        id = "3-mcp",
        name = "MCP action",
        description = "Published tool",
        contractType = ContractType.UNIT_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = "text",
        isTool = true,
    )

    private val all = listOf(groovyInOut, fileUnitUnitWithAlias, mcpUnitOut)

    @Test
    fun `text search matches id, name, description, data and alias`() {
        assertThat(search("1-groovy")).containsExactly(groovyInOut)
        assertThat(search("file action")).containsExactly(fileUnitUnitWithAlias)
        assertThat(search("reads a file")).containsExactly(fileUnitUnitWithAlias)
        assertThat(search("hello from data")).containsExactly(groovyInOut)
        assertThat(search("hello-alias")).containsExactly(fileUnitUnitWithAlias)
        assertThat(search("hello")).containsExactlyInAnyOrder(groovyInOut, fileUnitUnitWithAlias)
    }

    @Test
    fun `text search supports keyboard layout transformation`() {
        assertThat(search("руддщ")).containsExactlyInAnyOrder(groovyInOut, fileUnitUnitWithAlias)
        assertThat(searchTextTransformer.transformedText("руддщ")).containsExactly("руддщ", "hello")
    }

    @Test
    fun `empty search and reset return every action`() {
        assertThat(search("")).containsExactlyInAnyOrderElementsOf(all)
        assertThat(
            AtomicActionsFilter.filter(
                sortInfo = SortInfo(Direction.ASC, SortField.NAME),
                searchAction = SearchAction.Reset(emptyList()),
                actions = all,
            )
        ).containsExactlyInAnyOrderElementsOf(all)
    }

    @Test
    fun `sorts by name, contract and source`() {
        assertThat(filter(SortInfo(Direction.ASC, SortField.NAME)).map { it.id })
            .containsExactly("2-file", "1-groovy", "3-mcp")
        assertThat(filter(SortInfo(Direction.DESC, SortField.NAME)).map { it.id })
            .containsExactly("3-mcp", "1-groovy", "2-file")
        assertThat(filter(SortInfo(Direction.ASC, SortField.CONTRACT)).map { it.contractType })
            .containsExactly(ContractType.IN_OUT, ContractType.UNIT_OUT, ContractType.UNIT_UNIT)
        assertThat(filter(SortInfo(Direction.DESC, SortField.SOURCE)).map { it.source })
            .containsExactly(AtomicActionSource.TEXT, AtomicActionSource.TEXT, AtomicActionSource.FILE)
    }

    @Test
    fun `condition filters select contract, engine, source, MCP and scheduler`() {
        assertThat(conditions(SearchAction.SearchCondition.IN_OUT)).containsExactly(groovyInOut)
        assertThat(conditions(SearchAction.SearchCondition.UNIT_OUT)).containsExactly(mcpUnitOut)
        assertThat(conditions(SearchAction.SearchCondition.UNIT_UNIT)).containsExactly(fileUnitUnitWithAlias)
        assertThat(conditions(SearchAction.SearchCondition.GROOVY)).containsExactlyInAnyOrderElementsOf(all)
        assertThat(conditions(SearchAction.SearchCondition.TEXT)).containsExactlyInAnyOrder(groovyInOut, mcpUnitOut)
        assertThat(conditions(SearchAction.SearchCondition.FILE)).containsExactly(fileUnitUnitWithAlias)
        assertThat(conditions(SearchAction.SearchCondition.MCP)).containsExactly(mcpUnitOut)
        assertThat(
            AtomicActionsFilter.filter(
                sortInfo = SortInfo(Direction.ASC, SortField.NAME),
                searchAction = SearchAction.SearchInfo(
                    conditions = listOf(SearchAction.SearchCondition.SCHEDULER),
                    text = emptyList(),
                ),
                actions = all,
                scheduledActionIds = setOf(groovyInOut.id),
            )
        ).containsExactly(groovyInOut)
    }

    @Test
    fun `filters actions used for MCP`() {
        assertThat(conditions(SearchAction.SearchCondition.MCP)).containsExactly(mcpUnitOut)
    }

    @Test
    fun `filters actions used in scheduler`() {
        assertThat(
            AtomicActionsFilter.filter(
                sortInfo = SortInfo(direction = Direction.ASC, sortField = SortField.NAME),
                searchAction = searchAction(SearchAction.SearchCondition.SCHEDULER),
                actions = all,
                scheduledActionIds = setOf(groovyInOut.id),
            )
        ).containsExactly(groovyInOut)
    }

    @Test
    fun `combines MCP and scheduler conditions`() {
        val filtered = AtomicActionsFilter.filter(
            sortInfo = SortInfo(direction = Direction.ASC, sortField = SortField.NAME),
            searchAction = searchAction(
                SearchAction.SearchCondition.MCP,
                SearchAction.SearchCondition.SCHEDULER,
            ),
            actions = all,
            scheduledActionIds = setOf(mcpUnitOut.id),
        )

        assertThat(filtered).containsExactly(mcpUnitOut)
    }

    private fun searchAction(vararg conditions: SearchAction.SearchCondition) =
        SearchAction.SearchInfo(conditions = conditions.toList(), text = emptyList())

    private fun search(text: String): List<AtomicAction> =
        AtomicActionsFilter.filter(
            sortInfo = SortInfo(Direction.ASC, SortField.NAME),
            searchAction = SearchAction.SearchInfo(
                conditions = emptyList(),
                text = searchTextTransformer.transformedText(text),
            ),
            actions = all,
        )

    private fun filter(sortInfo: SortInfo): List<AtomicAction> =
        AtomicActionsFilter.filter(
            sortInfo = sortInfo,
            searchAction = SearchAction.SearchInfo(conditions = emptyList(), text = emptyList()),
            actions = all,
        )

    private fun conditions(condition: SearchAction.SearchCondition): List<AtomicAction> =
        AtomicActionsFilter.filter(
            sortInfo = SortInfo(Direction.ASC, SortField.NAME),
            searchAction = SearchAction.SearchInfo(conditions = listOf(condition), text = emptyList()),
            actions = all,
        )

    private fun atomic(
        id: String,
        name: String,
        description: String,
        contractType: ContractType,
        engine: AtomicActionEngine,
        source: AtomicActionSource,
        data: String,
        alias: String? = null,
        isTool: Boolean = false,
    ) = AtomicAction(
        id = id,
        name = name,
        description = description,
        contractType = contractType,
        engine = engine,
        source = source,
        data = data,
        alias = alias,
        isTool = isTool,
    )
}
