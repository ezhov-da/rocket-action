package ru.ezhov.rocket.action.api;

import java.awt.Component;

/**
 * UI action builder
 */
public interface RocketActionUi {
    /**
     * Component creation should only happen when this method is called.
     *
     * @return component to display
     */
    Component create(RocketActionSettings settings);

    /**
     * @return action type
     */
    String type();
}
