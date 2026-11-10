package ru.ezhov.rocket.action.application.chainaction.infrastructure.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.TestUtilsFactory

class AtomicActionDtoTest {
    private val objectMapper = TestUtilsFactory.objectMapper

    @Test
    fun `reads action written by an older version as an mcp tool`() {
        val dto = objectMapper.readValue(LEGACY_ATOMIC_ACTION, AtomicActionDto::class.java)

        assertThat(dto.isTool).isFalse()
    }

    @Test
    fun `reads and writes isTool flag`() {
        val dto = objectMapper.readValue(ATOMIC_ACTION_WITHOUT_TOOL, AtomicActionDto::class.java)

        assertThat(dto.isTool).isFalse()
        assertThat(objectMapper.writeValueAsString(dto)).contains("\"isTool\":false")
    }

    private companion object {
        private val LEGACY_ATOMIC_ACTION = """
            {
              "id": "action-id",
              "name": "Action name",
              "description": "Action description",
              "contractType": "IN_OUT",
              "engine": "GROOVY",
              "source": "TEXT",
              "data": "_INPUT",
              "alias": null,
              "icon": null
            }
        """.trimIndent()

        private val ATOMIC_ACTION_WITHOUT_TOOL = """
            {
              "id": "action-id",
              "name": "Action name",
              "description": "Action description",
              "contractType": "IN_OUT",
              "engine": "GROOVY",
              "source": "TEXT",
              "data": "_INPUT",
              "alias": null,
              "icon": null,
              "isTool": false
            }
        """.trimIndent()
    }
}
