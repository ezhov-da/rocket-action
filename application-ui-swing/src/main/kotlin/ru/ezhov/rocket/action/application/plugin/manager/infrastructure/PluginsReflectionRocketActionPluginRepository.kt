package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.ExtendedClassesPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.GroovyPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.InnerPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.JarsPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.KotlinPluginLoader
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class PluginsReflectionRocketActionPluginRepository : RocketActionPluginRepository {
    private var list: MutableList<RocketActionPlugin> = mutableListOf()

    private val innerPluginLoader: InnerPluginLoader = InnerPluginLoader()
    private val jarsPluginLoader: JarsPluginLoader = JarsPluginLoader()
    private val extendedClassesPluginLoader: ExtendedClassesPluginLoader = ExtendedClassesPluginLoader()
    private val groovyPluginLoader: GroovyPluginLoader = GroovyPluginLoader()
    private val kotlinPluginLoader: KotlinPluginLoader = KotlinPluginLoader()

    private fun load() = runBlocking {
        val times = measureTimeMillis {
            logger.info { "Initialise configuration rocket action repository" }
            // из jar-ников
            val fromJars = jarsPluginLoader
                .plugins()
                .map { jar ->
                    async { jarsPluginLoader.loadPlugin(jar) }
                }
                .awaitAll()
                .flatten()
                .toList()

            // внутренние
            val inner = innerPluginLoader
                .plugins()
                .mapNotNull { clazzName ->
                    async { innerPluginLoader.loadPlugin(clazzName) }
                }
                .awaitAll()
                .filterNotNull()
                .toList()

            // из class path
            val extended = extendedClassesPluginLoader
                .plugins()
                .map { clazzName ->
                    async {
                        innerPluginLoader.loadPlugin(clazzName)
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toList()

            // из groovy плагина
            val fromGroovy = groovyPluginLoader
                .plugins()
                .map { file ->
                    async {
                        groovyPluginLoader.loadPlugin(file)
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toList()

            // из kotlin плагина
            val fromKotlin = kotlinPluginLoader
                .plugins()
                .map { file ->
                    async {
                        kotlinPluginLoader.loadPlugin(file)
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toList()

            logger.info {
                "Load plugins. " +
                    "jars='${fromJars.size}' " +
                    "inner='${inner.size}' " +
                    "extended='${extended.size}' " +
                    "groovy='${fromGroovy.size}' " +
                    "kotlin='${fromKotlin.size}' "
            }

            val allPlugins = mutableListOf<RocketActionPlugin>()
            allPlugins.addAll(fromJars)
            allPlugins.addAll(inner)
            allPlugins.addAll(extended)
            allPlugins.addAll(fromGroovy)
            allPlugins.addAll(fromKotlin)

            list = allPlugins
        }
        logger.info { "Configuration rocket action repository initialize successful. timeMs=$times count=${list.size}" }
    }

    override fun all(): List<RocketActionPlugin> {
        if (list.isEmpty()) {
            load()
        }
        return list
    }

    override fun by(type: String): RocketActionPlugin? =
        all().firstOrNull { r: RocketActionPlugin ->
            r.configuration(context = RocketActionContextFactory.context).type().value() == type
        }
}


