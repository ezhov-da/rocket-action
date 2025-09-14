package ru.ezhov.rocket.action.application.search.infrastructure.plain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlainSearchEngineTest {
    private val plainSearchEngine = PlainSearchEngine()

    @Test
    fun `should be success when search by many words`() {
        plainSearchEngine.register("1", "Test word")
        plainSearchEngine.register("2", "Another word")

        val ids = plainSearchEngine.ids("est rd")

        assertThat(ids).isEqualTo(listOf("1"))
    }
}
