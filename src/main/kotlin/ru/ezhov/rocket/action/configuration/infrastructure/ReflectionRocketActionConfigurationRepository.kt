package ru.ezhov.rocket.action.configuration.infrastructure

import mu.KotlinLogging
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.configuration.domain.RocketActionConfigurationRepository
import java.lang.reflect.Modifier

private val logger = KotlinLogging.logger {}

class ReflectionRocketActionConfigurationRepository : RocketActionConfigurationRepository {
    private var list: MutableList<RocketActionConfiguration> = mutableListOf()

    fun load() {
        list = mutableListOf()
        val reflections = Reflections("", SubTypesScanner(true))
        val classes = reflections.getSubTypesOf(RocketActionConfiguration::class.java)
        for (aClass in classes) {
            try {
                if (!Modifier.isAbstract(aClass.modifiers)) {
                    list.add(aClass.getConstructor().newInstance() as RocketActionConfiguration)
                }
            } catch (e: InstantiationException) {
                logger.warn(e) { "Error when load class ${aClass.name}" }
            } catch (e: IllegalAccessException) {
                logger.warn(e) { "Error when load class ${aClass.name}" }
            } catch (e: NoSuchMethodException) {
                logger.warn(e) { "Error when load class ${aClass.name}" }
            }
        }
    }

    override fun all(): List<RocketActionConfiguration> = list

    override fun by(type: RocketActionType): RocketActionConfiguration? =
            all().firstOrNull { r: RocketActionConfiguration -> r.type().value() == type.value() }
}