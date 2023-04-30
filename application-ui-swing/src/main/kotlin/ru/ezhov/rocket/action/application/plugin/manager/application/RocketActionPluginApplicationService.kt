package ru.ezhov.rocket.action.application.plugin.manager.application

import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.PluginsReflectionRocketActionPluginRepository

class RocketActionPluginApplicationService {
    private val rocketActionPluginRepository = PluginsReflectionRocketActionPluginRepository()

    fun allSpec(): List<RocketActionPluginSpec> = rocketActionPluginRepository.all()

    fun all(): List<RocketActionPlugin> =
        rocketActionPluginRepository
            .all()
            .filterIsInstance(RocketActionPluginSpec.Success::class.java)
            .map { it.rocketActionPlugin }

    fun by(type: String): RocketActionPlugin? = rocketActionPluginRepository.by(type)?.rocketActionPlugin
}
