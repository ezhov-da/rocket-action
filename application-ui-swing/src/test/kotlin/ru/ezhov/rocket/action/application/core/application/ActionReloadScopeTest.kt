package ru.ezhov.rocket.action.application.core.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ActionReloadScopeTest {
    @Test
    fun `should render one line with every phase key and the collected counts`() {
        val scope = ActionReloadScope()
        scope.settingsRead(11)
        scope.cacheFilled(22)
        scope.componentsResolved(33)
        scope.swapped(44)
        scope.variablesLoaded(55)
        scope.mcpFinished(66)
        scope.loaded(actions = 2033, groups = 138)
        scope.evaluated()
        scope.evaluated()
        scope.evaluated()
        scope.cacheHit()
        scope.cacheHit()
        scope.cacheHit()
        scope.cacheHit()
        scope.failure()
        scope.failure()
        scope.iconHit()
        scope.iconMiss()
        scope.iconMiss()

        val line = scope.render()

        ActionReloadScope.METRIC_KEYS.forEach { key ->
            assertThat(line).describedAs("phase key '%s'", key).contains("$key=")
        }
        assertThat(line).contains("actions=2033")
        assertThat(line).contains("groups=138")
        assertThat(line).contains("settings=11ms")
        assertThat(line).contains("evaluations=3")
        assertThat(line).contains("variables=55ms")
        assertThat(line).contains("cacheFill=22ms")
        assertThat(line).contains("components=33ms")
        assertThat(line).contains("swap=44ms")
        assertThat(line).contains("mcp=66ms")
        assertThat(line).contains("iconHits=1")
        assertThat(line).contains("iconMisses=2")
        assertThat(line).contains("failures=2")
        assertThat(line).startsWith("Menu reload: ")
    }

    @Test
    fun `should report a timed out mcp refresh`() {
        val scope = ActionReloadScope()
        scope.swapped(10)

        assertThat(scope.render()).contains("mcp=timeout")
    }

    @Test
    fun `should evaluate settings once per action`() {
        val scope = ActionReloadScope()
        var evaluations = 0
        val evaluate = {
            evaluations++
            mapOf("label" to "value")
        }

        repeat(4) {
            assertThat(scope.settingsOf("action-1", evaluate))
                .isEqualTo(mapOf("label" to "value"))
        }

        assertThat(evaluations).isEqualTo(1)
        assertThat(scope.evaluations).isZero()
        assertThat(scope.cacheHits).isEqualTo(3)
    }

    @Test
    fun `should track changed actions for the reload`() {
        val scope = ActionReloadScope()
        scope.markChanged("child-1")

        assertThat(scope.isChanged("child-1")).isTrue()
        assertThat(scope.isChanged("child-2")).isFalse()
        assertThat(scope.isChangedAny(listOf("child-2", "child-1"))).isTrue()
    }
}
