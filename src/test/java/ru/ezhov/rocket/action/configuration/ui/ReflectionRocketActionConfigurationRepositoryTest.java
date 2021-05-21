package ru.ezhov.rocket.action.configuration.ui;

import org.junit.Test;
import ru.ezhov.rocket.action.api.RocketActionConfiguration;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionConfigurationRepository;

import java.util.List;

import static org.junit.Assert.assertFalse;

public class ReflectionRocketActionConfigurationRepositoryTest {

    @Test
    public void shouldCreateConfigurationList() {
        ReflectionRocketActionConfigurationRepository repository = new ReflectionRocketActionConfigurationRepository();

        final List<RocketActionConfiguration> all = repository.all();

        assertFalse(all.isEmpty());
    }

}