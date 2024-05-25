package ru.ezhov.rocket.action.api

interface RocketActionPluginInfo {
    fun version(): String

    fun author(): String

    fun link(): String?
}
