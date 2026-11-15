package ru.ezhov.rocket.action.application.handlers.server.mcp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class McpRefreshSchedulerTest {
    @Test
    fun `should return before the refresh completes and report its duration once`() {
        val runs = AtomicInteger()
        val executor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "mcp-refresh-test").apply { isDaemon = true }
        }
        val scheduler = McpRefreshScheduler(
            refresh = {
                Thread.sleep(500)
                runs.incrementAndGet()
            },
            executor = executor,
        )
        val scope = ActionReloadScope()

        try {
            val startedAt = System.nanoTime()
            scheduler.submit(scope)
            val submitMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

            // the caller is not blocked by the refresh
            assertThat(submitMs).isLessThan(250)
            assertThat(runs.get()).isZero()

            val reportedMs = scope.awaitMcp(5_000)

            assertThat(reportedMs).isNotNull()
            assertThat(runs.get()).isEqualTo(1)
        } finally {
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }
    }
}
