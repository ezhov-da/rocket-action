package ru.ezhov.rocket.action.application.engine.infrastructure

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable

internal class KotlinEngineTest {
    @Test
    fun `should execute script`() {
        val engine = KotlinEngine()
        val result = engine.execute(
            template = """
                |val t = "test"
                |val variables = _VARIABLES as Map<String, String>
                |t + "-" + testT + "-" + variables.get("testT")
            """.trimMargin(),
            variables = listOf(EngineVariable(name = "testT", value = "42"))
        )

        Assertions.assertThat(result).isEqualTo("test-42-42")
    }
}
