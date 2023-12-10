package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.ChainBasePanel

@Service
class ChainBasePanelFactory(
    actionExecutorService: ActionExecutorService,
    chainActionService: ChainActionService,
    atomicActionService: AtomicActionService,
    configurationApplication: ConfigurationApplication,
) : InitializingBean {
    var chainBasePanel: ChainBasePanel = ChainBasePanel(
        actionExecutorService = actionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
        configurationApplication = configurationApplication,
    )

    companion object {
        var INSTANCE: ChainBasePanel? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = chainBasePanel
    }

}
