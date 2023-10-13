package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSourceType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.RocketActionPluginDecorator
import java.io.File
import java.net.URLClassLoader
import java.time.Duration
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class JarsPluginLoader {
    fun plugins(): List<File> =
        File("plugins")
            .takeIf {
                logger.info { "Plugins folder='${it.absolutePath}' exists=${it.exists()}" }
                it.exists()
            }
            ?.listFiles { f -> f.name.endsWith(suffix = ".jar") }
            ?.toList()
            ?: emptyList()

    fun loadPlugin(jar: File): List<RocketActionPluginSpec> {
        val plugins = mutableListOf<RocketActionPluginSpec>()

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
                    val from = "JAR file '${jar.absolutePath}' resource path '$resourcePath' class '$clazzName'"
                    val sourceType = RocketActionPluginSourceType.JAR

                    try {
                        val ra: RocketActionPlugin
                        val initTimeClass = measureTimeMillis {
                            val clazz = classLoader.loadClass(clazzName)
                            val plugin = clazz.getDeclaredConstructor().newInstance() as RocketActionPlugin
                            ra = RocketActionPluginDecorator(rocketActionPluginOriginal = plugin)
                        }

                        logger.debug { "Initialize timeMs='$initTimeClass' for jar='${jar.absolutePath}'}" }

                        plugins.add(
                            RocketActionPluginSpec.Success(
                                rocketActionPlugin = ra,
                                from = from,
                                sourceType = sourceType,
                                loadTime = Duration.ofMillis(initTimeClass),
                            )
                        )
                    } catch (e: Exception) {
                        logger.warn(e) { "Error when load class '$clazzName' from file '${jar.absolutePath}'" }
                        plugins.add(
                            RocketActionPluginSpec.Failure(
                                from = from,
                                sourceType = sourceType,
                                error = "Error '${e.message}'",
                            )
                        )
                    }
                }
        }
        return plugins
    }
}
