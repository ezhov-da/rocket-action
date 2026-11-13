package ru.ezhov.rocket.action.application.chainaction.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ActionMcpToolNameTest {
    @Test
    fun `duplicate keeps the custom mcp tool name`() {
        val atomicAction = AtomicAction(
            id = "id",
            name = "Name",
            description = "Description",
            contractType = ContractType.IN_OUT,
            engine = AtomicActionEngine.GROOVY,
            source = AtomicActionSource.TEXT,
            data = "",
            mcpToolName = "custom_tool",
        )

        val duplicate = atomicAction.duplicate()

        assertThat(duplicate.mcpToolName()).isEqualTo("custom_tool")
        assertThat(duplicate.id()).isNotEqualTo(atomicAction.id())
    }

    @Test
    fun `duplicate keeps the custom mcp tool name of a chain action`() {
        val chainAction = ChainAction(
            id = "id",
            name = "Name",
            description = "Description",
            actions = emptyList(),
            mcpToolName = "custom_chain",
        )

        val duplicate = chainAction.duplicate()

        assertThat(duplicate.mcpToolName()).isEqualTo("custom_chain")
        assertThat(duplicate.id()).isNotEqualTo(chainAction.id())
    }

    @Test
    fun `action without a custom name reports null`() {
        val atomicAction = AtomicAction(
            id = "id",
            name = "Name",
            description = "Description",
            contractType = ContractType.IN_OUT,
            engine = AtomicActionEngine.GROOVY,
            source = AtomicActionSource.TEXT,
            data = "",
        )

        assertThat(atomicAction.mcpToolName()).isNull()
    }
}
