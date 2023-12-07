package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.engine.domain.Engine

interface ActionExecutor {
    companion object {
        const val INPUT_NAME_ARG = "_INPUT"
        const val ATOMIC_ACTION_EXECUTOR_ARG = "_AA"
    }

    fun execute(input: Any?, action: Action, progressExecutingAction: ProgressExecutingAction)

    fun additionalVariables(): List<AdditionalVariable> = listOf(
        AdditionalVariable(
            name = INPUT_NAME_ARG,
            description = "Variable storing input value",
        ),
        AdditionalVariable(
            name = Engine.VARIABLES_NAME,
            description = "All variables, both application and system",
        ),
        AdditionalVariable(
            name = ATOMIC_ACTION_EXECUTOR_ARG,
            description = "Object API for performing another action by alias. Call method `execute(\"alias\", arg)`",
        )
    )
}

data class AdditionalVariable(
    val name: String,
    val description: String,
)
