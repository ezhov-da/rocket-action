package ru.ezhov.rocket.action.application.handlers.server.mcp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType

class McpToolNameTest {
    @Test
    fun `generated name keeps the previous formula`() {
        assertThat(McpToolName.generated("0b7a-1234", "My Action"))
            .isEqualTo("My_Action__0b7a1234")
    }

    @Test
    fun `generated name transliterates cyrillic`() {
        assertThat(McpToolName.generated("action-id", "Привет мир"))
            .isEqualTo("Privet_mir__actionid")
    }

    @Test
    fun `blank custom name falls back to the generated name`() {
        assertThat(McpToolName.resolve("   ", "action-id", "Action"))
            .isEqualTo(McpToolName.generated("action-id", "Action"))
        assertThat(McpToolName.resolve(null, "action-id", "Action"))
            .isEqualTo(McpToolName.generated("action-id", "Action"))
    }

    @Test
    fun `custom name is trimmed transliterated and sanitized`() {
        assertThat(McpToolName.resolve("  Мой инструмент  ", "action-id", "Action"))
            .isEqualTo("Moy_instrument")
    }

    @Test
    fun `custom name keeps allowed characters`() {
        assertThat(McpToolName.normalize("my_Tool-1")).isEqualTo("my_Tool-1")
    }

    @Test
    fun `custom name is limited to 128 characters`() {
        val normalized = McpToolName.normalize("a".repeat(200))

        assertThat(normalized).hasSize(128)
    }

    @Test
    fun `conflict reports another action with the same resolved name`() {
        val existing = publishedAtomicAction(id = "other", mcpToolName = "same_tool")
        val edited = publishedAtomicAction(id = "edited", mcpToolName = "edited_tool")

        val conflict = McpToolName.conflict(" same_tool ", listOf(existing, edited), edited.id)

        assertThat(conflict).isSameAs(existing)
    }

    @Test
    fun `conflict ignores the action being edited`() {
        val edited = publishedAtomicAction(id = "edited", mcpToolName = "same_tool")

        val conflict = McpToolName.conflict("same_tool", listOf(edited), edited.id)

        assertThat(conflict).isNull()
    }

    @Test
    fun `conflict detects a custom name colliding with a generated one`() {
        val existing = publishedAtomicAction(id = "action-id", name = "Action")

        val conflict = McpToolName.conflict("Action__actionid", listOf(existing), null)

        assertThat(conflict).isSameAs(existing)
    }

    @Test
    fun `published keeps the first action for a duplicated name`() {
        val first = McpAction(publishedAtomicAction(id = "first", mcpToolName = "same_tool"), null)
        val second = McpAction(publishedAtomicAction(id = "second", mcpToolName = "same_tool"), null)

        val published = McpToolName.published(listOf(first, second))

        assertThat(published).containsExactly(first)
    }

    @Test
    fun `chain action uses its custom name`() {
        val chainAction = ChainAction(
            id = "chain-id",
            name = "Chain",
            description = "Description",
            actions = emptyList(),
            isTool = true,
            mcpToolName = "chain_tool",
        )

        assertThat(McpAction(chainAction, null).toolName).isEqualTo("chain_tool")
    }

    private fun publishedAtomicAction(
        id: String,
        name: String = "Action",
        mcpToolName: String? = null,
    ) = AtomicAction(
        id = id,
        name = name,
        description = "Description",
        contractType = ContractType.IN_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = "",
        isTool = true,
        mcpToolName = mcpToolName,
    )
}
