package ru.ezhov.rocket.action.infrastructure;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import ru.ezhov.rocket.action.api.RocketActionConfiguration;
import ru.ezhov.rocket.action.configuration.ui.RocketActionConfigurationRepository;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ReflectionRocketActionConfigurationRepository implements RocketActionConfigurationRepository {
    private List<RocketActionConfiguration> list;

    @Override
    public List<RocketActionConfiguration> all() {
        if (list == null) {
            list = new ArrayList<>();

            Reflections reflections = new Reflections("", new SubTypesScanner(true));
            final Set<Class<? extends RocketActionConfiguration>> classes = reflections.getSubTypesOf(RocketActionConfiguration.class);
            for (Class aClass : classes) {
                try {
                    if (!Modifier.isAbstract(aClass.getModifiers())) {
                        list.add((RocketActionConfiguration) aClass.newInstance());
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

    @Override
    public Optional<RocketActionConfiguration> by(String type) {
        final List<RocketActionConfiguration> all = all();
        return all.stream().filter(r -> r.type().equals(type)).findFirst();
    }
}
