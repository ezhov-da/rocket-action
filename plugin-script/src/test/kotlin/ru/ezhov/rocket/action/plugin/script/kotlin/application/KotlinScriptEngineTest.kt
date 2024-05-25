package ru.ezhov.rocket.action.plugin.script.kotlin.application

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class KotlinScriptEngineTest {

    @Test
    fun `should execute script`() {
        val engine = KotlinScriptEngine()
        val result = engine.execute(
            script = """
                |val t = "test"
                |val variables = _VARIABLES as Map<String, String>
                |t + "-" + testT + "-" + variables.get("testT")
            """.trimMargin(),
            variables = mapOf("testT" to "42")
        )
            .getOrHandle { throw it }

        Assertions.assertThat(result).isEqualTo("test-42-42")
    }
}

