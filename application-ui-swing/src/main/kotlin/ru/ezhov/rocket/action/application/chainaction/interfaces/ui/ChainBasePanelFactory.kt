package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.ChainBasePanel
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer

@Service
class ChainBasePanelFactory(
    actionExecutorService: ActionExecutorService,
    chainActionService: ChainActionService,
    atomicActionService: AtomicActionService,
    configurationApplication: ConfigurationApplication,
    searchTextTransformer: SearchTextTransformer,
    actionSchedulerService: ActionSchedulerService,
) : InitializingBean {
    var chainBasePanel: ChainBasePanel = ChainBasePanel(
        actionExecutorService = actionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
        configurationApplication = configurationApplication,
        searchTextTransformer = searchTextTransformer,
        actionSchedulerService = actionSchedulerService
    )

    companion object {
        var INSTANCE: ChainBasePanel? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = chainBasePanel
    }

}
