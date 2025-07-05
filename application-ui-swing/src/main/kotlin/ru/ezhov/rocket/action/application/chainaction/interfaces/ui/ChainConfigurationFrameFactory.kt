package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository

@Service
class ChainConfigurationFrameFactory(
    private val actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    private val actionExecutor: ActionExecutor,
    private val iconRepository: IconRepository,
    private val actionSchedulerService: ActionSchedulerService,
) : InitializingBean {

    companion object {
        var INSTANCE: ChainConfigurationFrame? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = ChainConfigurationFrame(
            actionExecutorService = actionExecutorService,
            chainActionService = chainActionService,
            atomicActionService = atomicActionService,
            actionExecutor = actionExecutor,
            iconRepository = iconRepository,
            actionSchedulerService = actionSchedulerService,
        )
    }
}
