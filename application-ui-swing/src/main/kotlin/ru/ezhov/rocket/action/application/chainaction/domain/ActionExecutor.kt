package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.Action

interface ActionExecutor {
    companion object {
        const val INPUT_NAME_ARG = "_INPUT"
    }

    fun execute(input: Any?, action: Action, progressExecutingAction: ProgressExecutingAction)
}
