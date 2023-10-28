package ru.ezhov.rocket.action.application.chainaction.application

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutorProgress

@Service
class ChainActionExecutorService(
    private val chainActionExecutor: ChainActionExecutor,
    private val chainActionService: ChainActionService,
) {
    fun execute(input: String, chainActionId: String, chainActionExecutorProgress: ChainActionExecutorProgress) {
        val chain = chainActionService.byId(chainActionId)!!
        chainActionExecutor.execute(input, chain, chainActionExecutorProgress)
    }
}
