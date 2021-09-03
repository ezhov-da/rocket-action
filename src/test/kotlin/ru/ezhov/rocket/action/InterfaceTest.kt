package ru.ezhov.rocket.action

import org.junit.Assert
import org.junit.Test
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import java.lang.reflect.Modifier

class InterfaceTest {
    @Test
    fun searchImplementationTest() {
        val reflections = Reflections("ru.ezhov", SubTypesScanner(true))
        val classes = reflections.getSubTypesOf(RocketActionConfiguration::class.java)
        for (aClass in classes) {
            println(aClass.toString() + " - " + Modifier.isAbstract(aClass.modifiers))
        }
        Assert.assertFalse(classes.isEmpty())
    }
}