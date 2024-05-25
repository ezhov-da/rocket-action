package ru.ezhov.rocket.action.application.plugin.manager.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec

private val logger = KotlinLogging.logger {}

@Service
class RocketActionPluginApplicationService(
    private val rocketActionPluginRepository: RocketActionPluginRepository
) {
    fun allSpec(): List<RocketActionPluginSpec> = rocketActionPluginRepository.all()

    fun all(): List<RocketActionPlugin> =
        rocketActionPluginRepository
            .all()
            .filterIsInstance(RocketActionPluginSpec.Success::class.java)
            .map { it.rocketActionPlugin }

    fun by(type: String): RocketActionPlugin? =
        rocketActionPluginRepository
            .by(type)
            ?.let { spec ->
                when (spec) {
                    is RocketActionPluginSpec.Success -> spec.rocketActionPlugin
                    is RocketActionPluginSpec.Failure -> {
                        logger.warn { "Error load plugin '$spec'" }
                        null
                    }
                }
            }
}
