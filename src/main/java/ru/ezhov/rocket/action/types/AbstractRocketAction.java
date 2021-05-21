package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfiguration;
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionUi;

/**
 * Base class for UI action
 */
public abstract class AbstractRocketAction implements RocketActionUi, RocketActionConfiguration {

    protected RocketActionConfigurationProperty createRocketActionProperty(String name, String description, boolean required) {
        return new RocketActionConfigurationProperty() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public boolean isRequired() {
                return required;
            }
        };
    }
}
