package ru.ezhov.rocket.action.application.handlers.server.mcp

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Runs MCP tool rebuilds on a single daemon thread.
 *
 * The caller (the Swing event subscriber) returns immediately, rebuilds are serialized, and
 * the duration is reported into the reload scope of [MenuContentReplacedDomainEvent].
 */
class McpRefreshScheduler(
    private val refresh: () -> Unit,
    private val executor: ExecutorService = defaultExecutor(),
) {
    fun submit(scope: ActionReloadScope?) {
        try {
            executor.execute {
                val startedAt = System.nanoTime()
                try {
                    refresh()
                } catch (ex: Exception) {
                    logger.error(ex) { "Can't refresh MCP tools" }
                } finally {
                    scope?.mcpFinished(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt))
                }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Can't schedule MCP tools refresh" }
            scope?.mcpFinished(0)
        }
    }

    companion object {
        fun defaultExecutor(): ExecutorService =
            Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "rocket-action-mcp-refresh").apply { isDaemon = true }
            }
    }
}
