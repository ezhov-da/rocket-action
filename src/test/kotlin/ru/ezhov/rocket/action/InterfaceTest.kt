package ru.ezhov.rocket.action

import org.junit.Assert
import org.junit.Test
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import java.lang.reflect.Modifier

class InterfaceTest {
    @Test
    fun searchImplementationTest() {
        val reflections = Reflections(
                ConfigurationBuilder()
                        .addUrls(ClasspathHelper.forPackage("ru.ezhov.rocket.action"))
                        .setScanners(Scanners.SubTypes)
        )
        val classes = reflections.getSubTypesOf(RocketActionConfiguration::class.java)
        for (aClass in classes) {
            println(aClass.toString() + " - " + Modifier.isAbstract(aClass.modifiers))
        }
        Assert.assertFalse(classes.isEmpty())
    }
}