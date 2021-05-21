package ru.ezhov.rocket.action.types;

import java.util.Map;

class ConfigurationUtil {
    public static String getValue(Map<String, String> configuration, String key) {
        return configuration.getOrDefault(key, "Not present " + key + " configuration");
    }
}
