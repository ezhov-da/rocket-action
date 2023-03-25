package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.RocketActionPluginDecorator
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class InnerPluginLoader {
    fun plugins(): List<String> =
        listOf(GroupRocketActionUi::class.java.canonicalName)

    fun loadPlugin(classAsName: String): RocketActionPlugin? {
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
}
