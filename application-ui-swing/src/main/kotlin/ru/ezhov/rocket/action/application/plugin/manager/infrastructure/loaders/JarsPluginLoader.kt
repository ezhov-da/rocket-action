package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.RocketActionPluginDecorator
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

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

    fun loadPlugin(jar: File): List<RocketActionPlugin> {
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
}
