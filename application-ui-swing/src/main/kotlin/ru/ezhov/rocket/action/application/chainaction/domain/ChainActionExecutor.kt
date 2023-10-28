package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction

interface ChainActionExecutor {
    companion object {
        const val INPUT_NAME_ARG = "_INPUT"
    }

    fun execute(input: Any?, chainAction: ChainAction, chainActionExecutorProgress: ChainActionExecutorProgress)
}
