package ru.ezhov.rocket.action.types

object ConfigurationUtil {
    fun getValue(configuration: Map<String, String>, key: String): String {
        return configuration.getOrDefault(key, "Not present $key configuration")
    }
}