package ru.ezhov.rocket.action.plugin.config

interface ConfigReader {
    fun name(): String
    fun description(): String
    fun nameBy(key: String): String
    fun descriptionBy(key: String): String
}
