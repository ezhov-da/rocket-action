package ru.ezhov.quick.action.types;

import java.util.Map;

class ConfigurationUtil {
    public static String getValue(Map<String, Object> configuration, String key) {
        return configuration.getOrDefault(key, "Not present " + key + " configuration").toString();
    }
}
