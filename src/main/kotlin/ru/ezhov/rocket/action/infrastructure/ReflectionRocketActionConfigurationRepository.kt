package ru.ezhov.rocket.action.infrastructure

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.configuration.ui.RocketActionConfigurationRepository
import java.lang.reflect.Modifier

class ReflectionRocketActionConfigurationRepository : RocketActionConfigurationRepository {
    private var list: MutableList<RocketActionConfiguration> = mutableListOf()
    override fun load() {
        list = mutableListOf()
        val reflections = Reflections("", SubTypesScanner(true))
        val classes = reflections.getSubTypesOf(RocketActionConfiguration::class.java)
        for (aClass in classes) {
            try {
                if (!Modifier.isAbstract(aClass.modifiers)) {
                    list.add(aClass.getConstructor().newInstance() as RocketActionConfiguration)
                }
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    override fun all(): List<RocketActionConfiguration> = list

    override fun by(type: String): RocketActionConfiguration? =
            all().firstOrNull { r: RocketActionConfiguration -> r.type() == type }
}