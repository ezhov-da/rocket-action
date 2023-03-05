package ru.ezhov.rocket.action.application.engine.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable

internal class MustacheEngineTest {
    @Test
    fun `should execute template`() {
        val result = MustacheEngine()
            .execute(
                "{{testKey}} world!",
                listOf(EngineVariable("testKey", "Hello"))
            )

        assertThat(result).isEqualTo("Hello world!")
    }
}
