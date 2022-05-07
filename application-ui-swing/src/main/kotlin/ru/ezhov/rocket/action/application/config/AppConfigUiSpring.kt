package ru.ezhov.rocket.action.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import ru.ezhov.rocket.action.application.BaseDialogClass
import ru.ezhov.rocket.action.application.domain.RocketActionComponentCache
import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.infrastructure.DbRocketActionSettingsRepository
import ru.ezhov.rocket.action.application.infrastructure.InMemoryRocketActionComponentCache
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.PluginsReflectionRocketActionPluginRepository
import ru.ezhov.rocket.action.application.properties.CommandLineAndResourceGeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.core.application.get.GetActionApplicationService
import ru.ezhov.rocket.action.core.application.get.GetActionSettingsApplicationService

@Configuration
@ComponentScan(basePackages = ["ru.ezhov.rocket.action"])
open class AppConfigUiSpring {
    @Bean
    open fun baseDialog(): BaseDialogClass = BaseDialogClass()

    @Bean
    open fun rocketActionComponentCache(): RocketActionComponentCache = InMemoryRocketActionComponentCache()

    @Bean
    open fun rocketActionPluginRepository(): RocketActionPluginRepository =
        PluginsReflectionRocketActionPluginRepository()

    @Bean
    open fun generalPropertiesRepository(): GeneralPropertiesRepository =
        CommandLineAndResourceGeneralPropertiesRepository()

    @Bean
    open fun rocketActionSettingsRepository(
        getActionApplicationService: GetActionApplicationService,
        getActionSettingsApplicationService: GetActionSettingsApplicationService,
    ): RocketActionSettingsRepository =
        DbRocketActionSettingsRepository(
            getActionApplicationService = getActionApplicationService,
            getActionSettingsApplicationService = getActionSettingsApplicationService,
        )
//
//    @Bean
//    open fun __(): __
//
//    @Bean
//    open fun __(): __
//
//    @Bean
//    open fun __(): __
//
//    @Bean
//    open fun __(): __
}
