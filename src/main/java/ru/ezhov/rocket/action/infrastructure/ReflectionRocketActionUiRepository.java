package ru.ezhov.rocket.action.infrastructure;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import ru.ezhov.rocket.action.RocketActionUiRepository;
import ru.ezhov.rocket.action.api.RocketActionUi;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ReflectionRocketActionUiRepository implements RocketActionUiRepository {
    private List<RocketActionUi> list;

    @Override
    public List<RocketActionUi> all() {
        if (list == null) {
            list = new ArrayList<>();

            Reflections reflections = new Reflections("", new SubTypesScanner(true));
            final Set<Class<? extends RocketActionUi>> classes = reflections.getSubTypesOf(RocketActionUi.class);
            for (Class aClass : classes) {
                try {
                    if (!Modifier.isAbstract(aClass.getModifiers())) {
                        list.add((RocketActionUi) aClass.newInstance());
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
    public Optional<RocketActionUi> by(String type) {
        final List<RocketActionUi> all = all();
        return all.stream().filter(r -> r.type().equals(type)).findFirst();
    }
}
