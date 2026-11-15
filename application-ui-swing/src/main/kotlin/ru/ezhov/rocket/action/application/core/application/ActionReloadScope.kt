package ru.ezhov.rocket.action.application.core.application

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger { }

/**
 * One action-menu reload.
 *
 * Owns the work that must not be repeated during a single reload:
 * - the effective settings of an action ([settingsOf]);
 * - the set of actions that were actually rebuilt ([markChanged] / [isChanged]).
 *
 * It also collects the phase durations and counters rendered into the single
 * `Menu reload: ...` log line, and it is the handle through which the
 * asynchronous MCP tool refresh reports its duration back to the reload.
 */
class ActionReloadScope(
    private val settingEvaluator: ((SettingsModel) -> Any)? = null,
) {
    private val startedAtNanos = System.nanoTime()

    /**
     * Applies the template engine to one setting (plain text is returned as is when no engine is wired).
     */
    fun evaluate(setting: SettingsModel): Any =
        settingEvaluator?.invoke(setting) ?: setting.value

    @Volatile
    private var finishedAtNanos: Long? = null

    @Volatile
    var settingsReadMs: Long = NOT_MEASURED
        private set

    @Volatile
    var cacheFillMs: Long = NOT_MEASURED
        private set

    @Volatile
    var componentsMs: Long = NOT_MEASURED
        private set

    @Volatile
    var swapMs: Long = NOT_MEASURED
        private set

    @Volatile
    var variablesMs: Long = NOT_MEASURED
        private set

    @Volatile
    var actions: Int = 0
        private set

    @Volatile
    var groups: Int = 0
        private set

    private val evaluationsCounter = AtomicInteger()
    private val cacheHitsCounter = AtomicInteger()
    private val failuresCounter = AtomicInteger()
    private val iconHitsCounter = AtomicInteger()
    private val iconMissesCounter = AtomicInteger()

    val evaluations: Int get() = evaluationsCounter.get()
    val cacheHits: Int get() = cacheHitsCounter.get()
    val failures: Int get() = failuresCounter.get()
    val iconHits: Int get() = iconHitsCounter.get()
    val iconMisses: Int get() = iconMissesCounter.get()

    private val settingsCache = ConcurrentHashMap<String, Map<String, String>>()
    private val changedIds = ConcurrentHashMap.newKeySet<String>()
    private val duplicateIds = mutableListOf<String>()

    private val mcpDuration = CompletableFuture<Long>()

    fun settingsRead(durationMs: Long) {
        settingsReadMs = durationMs
    }

    fun cacheFilled(durationMs: Long) {
        cacheFillMs = durationMs
    }

    fun componentsResolved(durationMs: Long) {
        componentsMs = durationMs
    }

    fun swapped(durationMs: Long) {
        swapMs = durationMs
        finishedAtNanos = System.nanoTime()
    }

    fun variablesLoaded(durationMs: Long) {
        variablesMs = durationMs
    }

    fun loaded(actions: Int, groups: Int) {
        this.actions = actions
        this.groups = groups
    }

    fun evaluated() {
        evaluationsCounter.incrementAndGet()
    }

    fun cacheHit() {
        cacheHitsCounter.incrementAndGet()
    }

    fun failure() {
        failuresCounter.incrementAndGet()
    }

    fun iconHit() {
        iconHitsCounter.incrementAndGet()
    }

    fun iconMiss() {
        iconMissesCounter.incrementAndGet()
    }

    fun duplicate(actionId: String) {
        synchronized(duplicateIds) {
            if (!duplicateIds.contains(actionId)) {
                duplicateIds.add(actionId)
            }
        }
    }

    fun duplicates(): List<String> = synchronized(duplicateIds) { duplicateIds.toList() }

    /**
     * Effective settings of one action. The first consumer evaluates them,
     * all the following consumers of the same reload get the same map.
     */
    fun settingsOf(actionId: String, evaluate: () -> Map<String, String>): Map<String, String> {
        settingsCache[actionId]?.let {
            cacheHit()
            return it
        }

        val evaluated = evaluate()
        val previous = settingsCache.putIfAbsent(actionId, evaluated)
        return previous ?: evaluated
    }

    fun markChanged(actionId: String) {
        changedIds.add(actionId)
    }

    val changed: Set<String> get() = changedIds

    fun isChanged(actionId: String): Boolean = changedIds.contains(actionId)

    fun isChangedAny(actionIds: Collection<String>): Boolean = actionIds.any { changedIds.contains(it) }

    /**
     * The MCP tool refresh runs off the reload path; it reports its duration here.
     */
    fun mcpFinished(durationMs: Long) {
        mcpDuration.complete(durationMs)
    }

    /**
     * @return the MCP refresh duration, or `null` when it did not finish within the timeout.
     */
    fun awaitMcp(timeoutMs: Long = MCP_WAIT_TIMEOUT_MS): Long? =
        try {
            mcpDuration.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (ex: TimeoutException) {
            null
        } catch (ex: Exception) {
            logger.warn(ex) { "Error when waiting for the MCP tools refresh" }
            null
        }

    fun totalMs(): Long =
        TimeUnit.NANOSECONDS.toMillis((finishedAtNanos ?: System.nanoTime()) - startedAtNanos)

    /**
     * Waits at most [MCP_WAIT_TIMEOUT_MS] for the MCP refresh and logs the single reload line.
     */
    fun log() {
        val mcpMs = awaitMcp()
        logger.info { render(mcpMs) }
        if (mcpMs == null) {
            logger.info { "MCP tools refresh is still running after '$MCP_WAIT_TIMEOUT_MS' ms" }
        }
    }

    fun render(mcpMs: Long? = mcpDuration.getNow(null)): String {
        val mcp = mcpMs?.let { "${it}ms" } ?: "timeout"
        return "Menu reload: " +
            "total=${totalMs()}ms " +
            "actions=$actions " +
            "groups=$groups " +
            "settings=${settingsReadMs}ms " +
            "evaluations=$evaluations " +
            "variables=${variablesMs}ms " +
            "cacheFill=${cacheFillMs}ms " +
            "components=${componentsMs}ms " +
            "swap=${swapMs}ms " +
            "mcp=$mcp " +
            "iconHits=$iconHits " +
            "iconMisses=$iconMisses " +
            "failures=$failures"
    }

    companion object {
        const val NOT_MEASURED: Long = -1L
        const val MCP_WAIT_TIMEOUT_MS: Long = 2_000L

        /**
         * Phase and counter keys that every rendered line contains.
         */
        val METRIC_KEYS = listOf(
            "total",
            "actions",
            "groups",
            "settings",
            "evaluations",
            "variables",
            "cacheFill",
            "components",
            "swap",
            "mcp",
            "iconHits",
            "iconMisses",
            "failures",
        )

        @Volatile
        private var current: ActionReloadScope? = null

        fun currentOrNull(): ActionReloadScope? = current

        fun current(): ActionReloadScope = current ?: ActionReloadScope()

        /**
         * Records the variables/password-store load into the running reload, when there is one.
         */
        fun recordVariablesLoad(durationMs: Long) {
            current?.variablesLoaded(durationMs)
        }

        /**
         * Runs [block] with [scope] visible through [currentOrNull] and restores the previous scope.
         */
        fun <T> withCurrent(scope: ActionReloadScope, block: () -> T): T {
            val previous = current
            current = scope
            return try {
                block()
            } finally {
                current = previous
            }
        }
    }
}
