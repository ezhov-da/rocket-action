package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction

interface ChainActionExecutor {
    fun execute(input: Any?, chainAction: ChainAction, chainActionExecutorProgress: ChainActionExecutorProgress)
}
