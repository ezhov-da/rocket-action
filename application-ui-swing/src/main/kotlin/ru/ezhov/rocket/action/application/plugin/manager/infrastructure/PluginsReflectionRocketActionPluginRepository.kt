package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import java.awt.Component
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class PluginsReflectionRocketActionPluginRepository : RocketActionPluginRepository {
    private var list: MutableList<RocketActionPlugin> = mutableListOf()

    private val innerPlugins = listOf(GroupRocketActionUi::class.java.canonicalName)

    private fun load() = runBlocking {
        val times = measureTimeMillis {
            logger.info { "Initialise configuration rocket action repository" }
            val fromJars = jars()
                .map { jar ->
                    async { loadPlugin(jar) }
                }
                .awaitAll()
                .flatten()
                .toMutableList()

            val inner = innerPlugins
                .mapNotNull { clazzName ->
                    async {
                        loadPlugin(clazzName)
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toMutableList()

            val extended = extendedPluginClassess()
                .mapNotNull { clazzName ->
                    async {
                        loadPlugin(clazzName)
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toMutableList()

            logger.info { "Load plugins. jars='${fromJars.size}' inner='${inner.size}' extended='${extended.size}'" }

            val allPlugins = mutableListOf<RocketActionPlugin>()
            allPlugins.addAll(fromJars)
            allPlugins.addAll(inner)
            allPlugins.addAll(extended)

            list = allPlugins
        }
        logger.info { "Configuration rocket action repository initialize successful. timeMs=$times count=${list.size}" }
    }

    private fun jars() =
        File("plugins")
            .takeIf {
                logger.info { "Plugins folder='${it.absolutePath}' exists=${it.exists()}" }
                it.exists()
            }
            ?.listFiles { f -> f.name.endsWith(suffix = ".jar") }
            ?: emptyArray()

    private fun loadPlugin(jar: File): List<RocketActionPlugin> {
        val plugins = mutableListOf<RocketActionPlugin>()
        val initTimeClass = measureTimeMillis {
            val resourcePath = "META-INF/rocket-action-plugins.properties"
            val jarFile = JarFile(jar)
            val jarEntry: JarEntry? = jarFile.getJarEntry(resourcePath)
            if (jarEntry == null) {
                logger.info { "File '${jar.absolutePath}' not contains resource '$resourcePath'" }
            } else {
                val text = jarFile.getInputStream(jarEntry)?.bufferedReader()?.use { it.readText() }
                val classLoader by lazy {
                    URLClassLoader.newInstance(arrayOf(jar.toURI().toURL()), this.javaClass.classLoader)
                }
                text
                    ?.lines()
                    ?.filter { it.isNotBlank() || it.isNotEmpty() }
                    ?.forEach { clazzName ->
                        try {
                            val clazz = classLoader.loadClass(clazzName)
                            val plugin = clazz.getDeclaredConstructor().newInstance() as RocketActionPlugin
                            plugins.add(RocketActionPluginDecorator(rocketActionPluginOriginal = plugin))
                        } catch (e: Exception) {
                            logger.warn(e) { "Error when load class '$clazzName' from file '${jar.absolutePath}'" }
                        }
                    }
            }
        }

        logger.debug { "Initialize timeMs='$initTimeClass' for jar='${jar.absolutePath}'}" }

        return plugins
    }

    private fun loadPlugin(classAsName: String): RocketActionPlugin? {
        var rap: RocketActionPlugin? = null
        val initTimeClass = measureTimeMillis {
            try {
                logger.debug { "Initialize class='$classAsName'} run..." }

                val clazz = Class.forName(classAsName)
                val plugin = clazz.newInstance() as RocketActionPlugin
                rap = RocketActionPluginDecorator(plugin)
            } catch (e: Exception) {
                logger.warn(e) { "Error when load class $classAsName" }
            }
        }

        logger.debug { "Initialize timeMs='$initTimeClass' for class='$classAsName'}" }

        return rap
    }

    private fun extendedPluginClassess(): List<String> =
        System.getProperty("rocket.action.extended.plugin.classess")
            ?.split(";")
            ?.map { it.trimIndent().trim() }
            ?: emptyList()

    override fun all(): List<RocketActionPlugin> {
        if (list.isEmpty()) {
            load();
        }
        return list
    }

    override fun by(type: RocketActionType): RocketActionPlugin? =
        all().firstOrNull { r: RocketActionPlugin -> r.configuration().type().value() == type.value() }

    private class RocketActionPluginDecorator(
        private val rocketActionPluginOriginal: RocketActionPlugin,
    ) : RocketActionPlugin {
        override fun factory(): RocketActionFactoryUi = RocketActionFactoryUiDecorator(
            rocketActionFactoryUi = rocketActionPluginOriginal.factory()
        )

        override fun configuration(): RocketActionConfiguration = rocketActionPluginOriginal.configuration()
    }

    private class RocketActionFactoryUiDecorator(
        private val rocketActionFactoryUi: RocketActionFactoryUi
    ) : RocketActionFactoryUi {
        override fun create(settings: RocketActionSettings): RocketAction? =
            rocketActionFactoryUi.create(settings = settings)
                ?.let { ra ->
                    when (val handlerFactory = ra as? RocketActionHandlerFactory) {
                        null -> RocketActionDecorator(originalRocketAction = ra)
                        else -> handlerFactory.handler()
                            ?.let { handler ->
                                RocketActionAndHandlerDecorator(
                                    originalRocketAction = ra,
                                    originalRocketActionHandler = handler,
                                )
                            }
                            ?: run {
                                logger.info {
                                    "${ra.javaClass.name} implement " +
                                        "${RocketActionHandlerFactory::class.java.name}, " +
                                        "but handler is null"
                                }
                                RocketActionDecorator(originalRocketAction = ra)
                            }
                    }
                }

        override fun type(): RocketActionType = rocketActionFactoryUi.type()
    }

    private open class RocketActionDecorator(
        private val originalRocketAction: RocketAction
    ) : RocketAction {
        companion object {
            const val MAX_TIME_GET_COMPONENT_IN_MILLS = 2
        }

        override fun contains(search: String): Boolean = originalRocketAction.contains(search = search)

        override fun isChanged(actionSettings: RocketActionSettings): Boolean =
            originalRocketAction.isChanged(actionSettings = actionSettings)

        override fun component(): Component {
            val component: Component
            val timeInMillis = measureTimeMillis {
                component = originalRocketAction.component()
            }

            if (timeInMillis > MAX_TIME_GET_COMPONENT_IN_MILLS) {
                logger.warn {
                    "Getting component for action was over '$MAX_TIME_GET_COMPONENT_IN_MILLS' milliseconds. " +
                        "This can slow down the application"
                }
            }

            return component
        }
    }

    private class RocketActionAndHandlerDecorator(
        private val originalRocketActionHandler: RocketActionHandler,
        originalRocketAction: RocketAction,
    ) : RocketActionDecorator(originalRocketAction), RocketActionHandler {
        override fun id(): String = originalRocketActionHandler.id()

        override fun contracts(): List<RocketActionHandlerCommandContract> = originalRocketActionHandler.contracts()

        override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus =
            originalRocketActionHandler.handle(command = command)
    }
}


