package ru.ezhov.rocket.action.api;

import java.util.List;
import java.util.Map;

/**
 * Action settings
 */
public interface RocketActionSettings {
    /**
     * @return configurable action id
     */
    String id();

    /**
     * @return configurable action type
     */
    String type();

    /**
     * @return action settings
     */
    Map<String, String> settings();

    /**
     * @return configured child actions
     */
    List<RocketActionSettings> actions();
}
