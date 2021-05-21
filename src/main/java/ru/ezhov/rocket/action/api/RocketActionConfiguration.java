package ru.ezhov.rocket.action.api;

import java.util.List;

/**
 * Configuring actions
 */
public interface RocketActionConfiguration {
    /**
     * @return configurable action type
     */
    String type();

    /**
     * @return configurable action description
     */
    String description();

    /**
     * @return list of action properties to configure
     */
    List<RocketActionConfigurationProperty> properties();
}
