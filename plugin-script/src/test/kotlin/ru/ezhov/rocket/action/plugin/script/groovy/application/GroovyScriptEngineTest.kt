package ru.ezhov.rocket.action.plugin.script.groovy.application

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GroovyScriptEngineTest {
    @Test
    fun `should evaluate groovy script`() {
        val result = GroovyScriptEngine()
            .execute(
                script = "\"\${2 * 2 + test} from map:\${_VARIABLES['test']}\"",
                variables = mapOf("test" to "45")
            )

        assertThat(result.getOrHandle { throw it }.toString()).isEqualTo("445 from map:45")
    }
}
