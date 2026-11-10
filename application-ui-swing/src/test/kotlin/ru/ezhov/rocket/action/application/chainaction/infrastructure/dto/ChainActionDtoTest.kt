package ru.ezhov.rocket.action.application.chainaction.infrastructure.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.TestUtilsFactory

class ChainActionDtoTest {
    private val objectMapper = TestUtilsFactory.objectMapper

    @Test
    fun `reads chain action written by an older version without isTool flag`() {
        val dto = objectMapper.readValue(LEGACY_CHAIN_ACTION, ChainActionDto::class.java)

        assertThat(dto.isTool).isFalse()
    }

    @Test
    fun `reads and writes isTool flag`() {
        val dto = objectMapper.readValue(CHAIN_ACTION_WITH_IS_TOOL, ChainActionDto::class.java)

        assertThat(dto.isTool).isTrue()
        assertThat(objectMapper.writeValueAsString(dto)).contains("\"isTool\":true")
    }

    private companion object {
        private val LEGACY_CHAIN_ACTION = """
            {
              "id": "chain-id",
              "name": "Chain name",
              "description": "Chain description",
              "actions": [],
              "icon": null
            }
        """.trimIndent()

        private val CHAIN_ACTION_WITH_IS_TOOL = """
            {
              "id": "chain-id",
              "name": "Chain name",
              "description": "Chain description",
              "actions": [],
              "icon": null,
              "isTool": true
            }
        """.trimIndent()
    }
}
