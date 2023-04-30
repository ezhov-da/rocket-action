package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.ClassPathPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.GroovyPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.InnerPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.JarsPluginLoader
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders.KotlinPluginLoader
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class PluginsReflectionRocketActionPluginRepository : RocketActionPluginRepository {
    private var list: MutableList<RocketActionPluginSpec> = mutableListOf()

    private val innerPluginLoader: InnerPluginLoader = InnerPluginLoader()
    private val jarsPluginLoader: JarsPluginLoader = JarsPluginLoader()
    private val classPathPluginLoader: ClassPathPluginLoader = ClassPathPluginLoader()
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
                .map { clazzName ->
                    async { innerPluginLoader.loadPlugin(clazzName) }
                }
                .awaitAll()
                .toList()

            // из class path
            val extended = classPathPluginLoader
                .plugins()
                .map { clazzName ->
                    async {
                        classPathPluginLoader.loadPlugin(clazzName)
                    }
                }
                .awaitAll()
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
                .toList()

            logger.info {
                "Load plugins. " +
                    "jars='${fromJars.size}' " +
                    "inner='${inner.size}' " +
                    "extended='${extended.size}' " +
                    "groovy='${fromGroovy.size}' " +
                    "kotlin='${fromKotlin.size}' "
            }

            val allPlugins = mutableListOf<RocketActionPluginSpec>()
            allPlugins.addAll(fromJars)
            allPlugins.addAll(inner)
            allPlugins.addAll(extended)
            allPlugins.addAll(fromGroovy)
            allPlugins.addAll(fromKotlin)

            list = allPlugins
        }
        logger.info { "Configuration rocket action repository initialize successful. timeMs=$times count=${list.size}" }
    }

    override fun all(): List<RocketActionPluginSpec> {
        if (list.isEmpty()) {
            load()
        }
        return list
    }

    override fun by(type: String): RocketActionPluginSpec.Success? =
        all()
            .filterIsInstance(RocketActionPluginSpec.Success::class.java)
            .firstOrNull { r: RocketActionPluginSpec.Success ->
                r.rocketActionPlugin.configuration(context = RocketActionContextFactory.context)
                    .type()
                    .value() == type
            }
}


