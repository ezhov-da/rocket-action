package ru.ezhov.rocket.action.application.plugin.manager.domain

import ru.ezhov.rocket.action.api.RocketActionPlugin
import java.time.Duration

sealed class RocketActionPluginSpec {
    data class Success(
        val rocketActionPlugin: RocketActionPlugin,
        val from: String,
        val version: String,
        val author: String,
        val link: String?,
        val sourceType: RocketActionPluginSourceType,
        val loadTime: Duration,
    ) : RocketActionPluginSpec()

    data class Failure(
        val from: String,
        val sourceType: RocketActionPluginSourceType,
        val error: String,
    ) : RocketActionPluginSpec()
}
