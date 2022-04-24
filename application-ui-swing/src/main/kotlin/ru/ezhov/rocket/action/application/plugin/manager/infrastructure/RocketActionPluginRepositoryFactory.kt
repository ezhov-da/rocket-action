package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository

object RocketActionPluginRepositoryFactory {
    val repository: RocketActionPluginRepository = PluginsReflectionRocketActionPluginRepository()
}
