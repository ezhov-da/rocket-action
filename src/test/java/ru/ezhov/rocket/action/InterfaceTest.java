package ru.ezhov.rocket.action;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import ru.ezhov.rocket.action.api.RocketActionConfiguration;

import java.lang.reflect.Modifier;
import java.util.Set;

import static org.junit.Assert.assertFalse;

public class InterfaceTest {
    @Test
    public void searchImplementationTest() {
        Reflections reflections = new Reflections("ru.ezhov", new SubTypesScanner(true));
        final Set<Class<? extends RocketActionConfiguration>> classes = reflections.getSubTypesOf(RocketActionConfiguration.class);
        for (Class aClass : classes) {
            System.out.println(aClass + " - " + Modifier.isAbstract(aClass.getModifiers()));
        }

        assertFalse(classes.isEmpty());
    }
}
