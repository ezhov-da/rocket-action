package ru.ezhov.rocket.action.application.engine.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.engine.domain.model.EngineVariable
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class GroovyEngineCacheTest {
    @Test
    fun `should compile the same template once`() {
        val cache = GroovyScriptClassCache()
        val engine = GroovyEngine(cache)

        assertThat(engine.execute("\"value\"", emptyList())).isEqualTo("value")
        assertThat(engine.execute("\"value\"", emptyList())).isEqualTo("value")

        assertThat(cache.compileCount).isEqualTo(1)
        assertThat(cache.classFor("\"value\"")).isSameAs(cache.classFor("\"value\""))
    }

    @Test
    fun `should expose variables as binding properties and through the variables map`() {
        val engine = GroovyEngine()

        val result = engine.execute(
            template = "\"\$first|\${_VARIABLES['second']}\"",
            variables = listOf(
                EngineVariable(name = "first", value = "one"),
                EngineVariable(name = "second", value = "two"),
            ),
        )

        assertThat(result.toString()).isEqualTo("one|two")
    }

    @Test
    fun `should use a fresh binding for every execution`() {
        val engine = GroovyEngine()
        val template = "counter = counter + 1; counter"
        val variables = listOf(EngineVariable(name = "counter", value = 0))

        val first = engine.execute(template, variables)
        val second = engine.execute(template, variables)

        // a shared binding would leak the increment from the first run
        assertThat(first).isEqualTo(1)
        assertThat(second).isEqualTo(1)
    }

    @Test
    fun `should evaluate concurrently`() {
        val engine = GroovyEngine()
        val executor = Executors.newFixedThreadPool(8)
        try {
            val tasks = (1..32).map { index ->
                Callable {
                    engine.execute(
                        template = "\"\$left-\$right\"",
                        variables = listOf(
                            EngineVariable(name = "left", value = index.toString()),
                            EngineVariable(name = "right", value = "x"),
                        ),
                    ).toString()
                }
            }

            val results = executor.invokeAll(tasks).map { it.get() }

            assertThat(results).containsExactlyElementsOf((1..32).map { "$it-x" })
        } finally {
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `should evict beyond the bound and still evaluate an evicted template`() {
        val cache = GroovyScriptClassCache()
        val engine = GroovyEngine(cache)

        repeat(260) { index ->
            assertThat(engine.execute("\"value-$index\"", emptyList())).isEqualTo("value-$index")
        }

        // ten more templates than the bound, so the first ones were evicted
        assertThat(cache.compileCount).isEqualTo(260)

        // an evicted template is recompiled transparently
        assertThat(engine.execute("\"value-0\"", emptyList())).isEqualTo("value-0")
        assertThat(cache.compileCount).isEqualTo(261)
    }
}
