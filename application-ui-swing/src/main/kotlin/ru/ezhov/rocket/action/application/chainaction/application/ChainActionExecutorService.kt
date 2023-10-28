package ru.ezhov.rocket.action.application.chainaction.application

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutorProgress

@Service
class ChainActionExecutorService(
    private val chainActionExecutor: ChainActionExecutor
) {
    fun execute(input: Any?, chainActionId: String, chainActionExecutorProgress: ChainActionExecutorProgress) {

    }
}
