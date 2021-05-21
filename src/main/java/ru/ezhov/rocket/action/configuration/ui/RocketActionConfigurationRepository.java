package ru.ezhov.rocket.action.configuration.ui;

import ru.ezhov.rocket.action.api.RocketActionConfiguration;

import java.util.List;
import java.util.Optional;

public interface RocketActionConfigurationRepository {
    List<RocketActionConfiguration> all();

    Optional<RocketActionConfiguration> by(String type);
}
