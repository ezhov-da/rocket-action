package ru.ezhov.rocket.action.application.chainaction.infrastructure

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor


@Service
class ActionExecutorFactory(
    private val actionExecutor: ActionExecutor,
) : InitializingBean {
    /**
     * Used only for access outside the Spring context.
     * Important! Can be null if called before the context is initialized.
     */
    companion object {
        var INSTANCE: ActionExecutor? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = actionExecutor
    }
}
