package ru.ezhov.rocket.action.application.beanconfig

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import ru.ezhov.rocket.action.application.CommandLineArgsSingleton
import ru.ezhov.rocket.action.application.UiQuickActionService
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.domain.RocketActionComponentCache
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.core.infrastructure.yml.YmlRocketActionSettingsRepository
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File

private val logger = KotlinLogging.logger { }

@Configuration
@ComponentScan(basePackages = ["ru.ezhov.rocket.action"])
open class AppConfigCoreSpring {
    /**
     * Usage for sync in Group plugin
     */
    @Bean
    open fun rocketActionComponentCache(): RocketActionComponentCache = RocketActionComponentCacheFactory.cache

    @Bean
    open fun rocketActionSettingsRepository(
        generalPropertiesRepository: GeneralPropertiesRepository,
        engineService: EngineService,
    ): RocketActionSettingsRepository =
        if (generalPropertiesRepository.asBoolean(UsedPropertiesName.IS_DEVELOPER, false)) {
            val testActions = "/test-actions.yml"

            logger.info { "Develop mode enabled. absolute path to `$testActions` file as argument" }

            YmlRocketActionSettingsRepository(
                uri = UiQuickActionService::class.java.getResource(testActions).toURI(),
                engineService
            )
        } else {
            CommandLineArgsSingleton.args!!.let { args ->
                val path = if (args.isNotEmpty()) {
                    args[0]
                } else {
                    generalPropertiesRepository.asString(
                        name = UsedPropertiesName.DEFAULT_ACTIONS_FILE,
                        default = "./actions.yml"
                    )
                }
                val file = File(path)
                val repository = YmlRocketActionSettingsRepository(uri = file.toURI(), engineService)
                if (file.exists()) {
                    logger.info { "File '${file.absolutePath}' with actions exists" }
                } else {
                    repository.save(ActionsModel(actions = emptyList()))
                    logger.info { "File '${file.absolutePath}' with actions created" }
                }
                repository
            }
        }
}
