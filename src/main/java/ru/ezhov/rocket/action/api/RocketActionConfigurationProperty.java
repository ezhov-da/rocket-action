package ru.ezhov.rocket.action.api;

/**
 * Property awaiting action
 */
public interface RocketActionConfigurationProperty {
    /**
     * @return property name
     */
    String name();

    /**
     * @return property description
     */
    String description();

    /**
     * @return mandatory property
     */
    boolean isRequired();
}
