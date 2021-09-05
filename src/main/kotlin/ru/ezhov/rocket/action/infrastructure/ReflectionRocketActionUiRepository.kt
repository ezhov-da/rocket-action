package ru.ezhov.rocket.action.infrastructure

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import ru.ezhov.rocket.action.domain.RocketActionUiRepository
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import java.lang.reflect.Modifier

class ReflectionRocketActionUiRepository : RocketActionUiRepository {
    private var list: MutableList<RocketActionFactoryUi> = mutableListOf()
    fun load() {
        val reflections = Reflections("", SubTypesScanner(true))
        val classes = reflections.getSubTypesOf(RocketActionFactoryUi::class.java)
        for (aClass in classes) {
            try {
                if (!Modifier.isAbstract(aClass.modifiers)) {
                    list.add(aClass.getConstructor().newInstance() as RocketActionFactoryUi)
                }
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    override fun all(): List<RocketActionFactoryUi> = list

    override fun by(type: String): RocketActionFactoryUi? =
            all().firstOrNull { r: RocketActionFactoryUi -> r.type() == type }
}