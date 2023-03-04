package ru.ezhov.rocket.action.plugin.script.kotlin.application

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class KotlinScriptEngineTest {

    @Test
    fun `should execute script`() {
        val engine = KotlinScriptEngine()
        val result = engine.execute("val t = \"test\"; t").getOrHandle { throw it }

        assertThat(result).isEqualTo("test")
    }
}
