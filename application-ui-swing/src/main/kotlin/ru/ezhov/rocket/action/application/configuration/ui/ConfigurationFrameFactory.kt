package ru.ezhov.rocket.action.application.configuration.ui

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.about.AboutDialogFactory
import ru.ezhov.rocket.action.application.availableproperties.AvailablePropertiesFromCommandLineDialogFactory
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeysApplication
import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import ru.ezhov.rocket.action.application.handlers.server.HttpServerService
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication

@Service
class ConfigurationFrameFactory(
    rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    rocketActionSettingsService: RocketActionSettingsService,
    rocketActionContextFactory: RocketActionContextFactory,
    engineService: EngineService,
    availableHandlersRepository: AvailableHandlersRepository,
    tagsService: TagsService,
    generalPropertiesRepository: GeneralPropertiesRepository,
    variablesApplication: VariablesApplication,
    aboutDialogFactory: AboutDialogFactory,
    httpServerService: HttpServerService,
    availablePropertiesFromCommandLineDialogFactory: AvailablePropertiesFromCommandLineDialogFactory,
    apiKeysApplication: ApiKeysApplication,
    searchTextTransformer: SearchTextTransformer,
) : InitializingBean {
    val configurationFrame = ConfigurationFrame(
        rocketActionPluginApplicationService = rocketActionPluginApplicationService,
        rocketActionSettingsService = rocketActionSettingsService,
        rocketActionContextFactory = rocketActionContextFactory,
        engineService = engineService,
        availableHandlersRepository = availableHandlersRepository,
        tagsService = tagsService,
        generalPropertiesRepository = generalPropertiesRepository,
        variablesApplication = variablesApplication,
        aboutDialogFactory = aboutDialogFactory,
        httpServerService = httpServerService,
        availablePropertiesFromCommandLineDialogFactory = availablePropertiesFromCommandLineDialogFactory,
        apiKeysApplication = apiKeysApplication,
        searchTextTransformer = searchTextTransformer,
    )


    companion object {
        var INSTANCE: ConfigurationFrame? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = configurationFrame
    }
}
