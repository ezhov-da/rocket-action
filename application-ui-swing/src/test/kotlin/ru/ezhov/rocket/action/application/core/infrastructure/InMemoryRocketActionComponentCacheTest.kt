package ru.ezhov.rocket.action.application.core.infrastructure

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCached

internal class InMemoryRocketActionComponentCacheTest {
    @Test
    fun `should return only the requested entries and ignore unknown ids`() {
        val cache = InMemoryRocketActionComponentCache()
        val first = RocketActionCached.newRocketAction(mockk<RocketAction>())
        val second = RocketActionCached.newRocketAction(mockk<RocketAction>())
        cache.add("first", first)
        cache.add("second", second)

        val result = cache.byIds(linkedSetOf("second", "unknown", "first"))

        assertThat(result).containsExactly(second, first)
    }

    @Test
    fun `should return an empty list when nothing matches`() {
        val cache = InMemoryRocketActionComponentCache()

        assertThat(cache.byIds(setOf("missing"))).isEmpty()
    }
}
