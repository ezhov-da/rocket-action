package ru.ezhov.rocket.action.application.engine.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable

internal class GroovyEngineTest {
    @Test
    fun `should execute groovy script`() {
        val engine = GroovyEngine()

        val result = engine.execute(
            template = "\"\$test \$test2\"",
            variables = listOf(
                EngineVariable(
                    name = "test",
                    value = "Hello",
                ),
                EngineVariable(
                    name = "test2",
                    value = "world!",
                )
            )
        )

        assertThat(result).isEqualTo("Hello world!")
    }

    @Test
    fun `should execute groovy script with variables with dot but not usage`() {
        val engine = GroovyEngine()

        val result = engine.execute(
            template = "\"\$test\"",
            variables = listOf(
                EngineVariable(
                    name = "test",
                    value = "Hello",
                ),
                EngineVariable(
                    name = "test2.test",
                    value = "world!",
                )
            )
        )

        assertThat(result).isEqualTo("Hello")
    }
}
