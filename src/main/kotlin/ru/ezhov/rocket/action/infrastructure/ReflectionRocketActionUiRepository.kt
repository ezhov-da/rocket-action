package ru.ezhov.rocket.action.infrastructure

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import ru.ezhov.rocket.action.RocketActionUiRepository
import ru.ezhov.rocket.action.api.RocketActionUi
import java.lang.reflect.Modifier

class ReflectionRocketActionUiRepository : RocketActionUiRepository {
    private var list: MutableList<RocketActionUi> = mutableListOf()
    override fun load() {
        val reflections = Reflections("", SubTypesScanner(true))
        val classes = reflections.getSubTypesOf(RocketActionUi::class.java)
        for (aClass in classes) {
            try {
                if (!Modifier.isAbstract(aClass.modifiers)) {
                    list.add(aClass.getConstructor().newInstance() as RocketActionUi)
                }
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    override fun all(): List<RocketActionUi> = list

    override fun by(type: String): RocketActionUi? =
            all().firstOrNull { r: RocketActionUi -> r.type() == type }
}