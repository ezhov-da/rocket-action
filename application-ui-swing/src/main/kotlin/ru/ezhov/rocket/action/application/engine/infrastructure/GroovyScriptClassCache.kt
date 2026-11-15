package ru.ezhov.rocket.action.application.engine.infrastructure

import groovy.lang.GroovyClassLoader
import java.util.concurrent.atomic.AtomicInteger

/**
 * Compiles a Groovy template once per process run.
 *
 * A single class loader is shared, so names resolve the same way for every template. The map is
 * a bounded LRU: classes already loaded stay in the JVM, eviction only costs one recompilation
 * if a template is used again.
 */
class GroovyScriptClassCache(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {
    private val classLoader = GroovyClassLoader(GroovyScriptClassCache::class.java.classLoader)
    private val lock = Any()
    private val compileCounter = AtomicInteger()

    private val cache = object : LinkedHashMap<String, Class<*>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Class<*>>): Boolean =
            size > maxEntries
    }

    fun classFor(template: String): Class<*> = synchronized(lock) {
        cache[template]?.let { return it }

        val compiled = classLoader.parseClass(template)
        compileCounter.incrementAndGet()
        cache[template] = compiled
        compiled
    }

    /**
     * Number of compilations performed by this cache; used by tests to prove template reuse.
     */
    val compileCount: Int get() = compileCounter.get()

    companion object {
        const val DEFAULT_MAX_ENTRIES = 256
    }
}
