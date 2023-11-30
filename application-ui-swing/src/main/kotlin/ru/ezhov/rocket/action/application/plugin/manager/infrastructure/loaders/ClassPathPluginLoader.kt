package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSourceType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.RocketActionPluginDecorator
import java.time.Duration
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class ClassPathPluginLoader {
    companion object {
        private const val PROPERTY_NAME = "rocket.action.extended.plugin.classess"
        private const val DELIMITER = ";"
    }

    fun plugins(): List<String> =
        System.getProperty(PROPERTY_NAME)
            ?.split(DELIMITER)
            ?.map { it.trimIndent().trim() }
            ?: emptyList()

    fun loadPlugin(classAsName: String): RocketActionPluginSpec {
        val from = "Property name '$PROPERTY_NAME', delimiter '$DELIMITER', class name '$classAsName'"
        val sourceType = RocketActionPluginSourceType.CLASS_PATH

        return try {
            var rocketActionPlugin: RocketActionPlugin
            val initTimeClass = measureTimeMillis {
                logger.debug { "Initialize class='$classAsName'} run..." }

                val clazz = Class.forName(classAsName)
                val plugin = clazz.newInstance() as RocketActionPlugin
                rocketActionPlugin = RocketActionPluginDecorator(plugin)
            }

            logger.debug { "Initialize timeMs='$initTimeClass' for class='$classAsName'}" }

            RocketActionPluginSpec.Success(
                rocketActionPlugin = rocketActionPlugin,
                from = from,
                version = rocketActionPlugin.info().version(),
                author = rocketActionPlugin.info().author(),
                link = rocketActionPlugin.info().link(),
                sourceType = sourceType,
                loadTime = Duration.ofMillis(initTimeClass),
            )
        } catch (e: Exception) {
            logger.warn(e) { "Error when load class $classAsName" }
            RocketActionPluginSpec.Failure(
                from = from,
                sourceType = sourceType,
                error = "Error '${e.message}'"
            )
        }
    }
}
