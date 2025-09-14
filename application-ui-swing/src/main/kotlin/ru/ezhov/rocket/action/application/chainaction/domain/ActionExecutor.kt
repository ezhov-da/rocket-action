package ru.ezhov.rocket.action.application.chainaction.domain

import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.engine.domain.Engine

interface ActionExecutor {
    companion object {
        const val INPUT_ARG_NAME = "_INPUT"
        const val ATOMIC_ACTION_EXECUTOR_ARG_NAME = "_AA"
        const val ATOMIC_ACTION_VARIABLE_UPDATER_ARG_NAME = "_V"
    }

    fun execute(input: Any?, action: Action, progressExecutingAction: ProgressExecutingAction)

    fun additionalVariables(): List<AdditionalVariable> = listOf(
        AdditionalVariable(
            name = INPUT_ARG_NAME,
            description = "Variable storing input value",
        ),
        AdditionalVariable(
            name = Engine.VARIABLES_NAME,
            description = "All variables, both application and system. Map of variables",
        ),
        AdditionalVariable(
            name = ATOMIC_ACTION_EXECUTOR_ARG_NAME,
            description = "Object API for performing another action by alias. " +
                "Call method `${ATOMIC_ACTION_EXECUTOR_ARG_NAME}.execute(\"alias\", arg)`",
        ),
        AdditionalVariable(
            name = ATOMIC_ACTION_VARIABLE_UPDATER_ARG_NAME,
            description = "Object API for update variable. " +
                "Call method `${ATOMIC_ACTION_VARIABLE_UPDATER_ARG_NAME}.updateValue(\"name\", \"value\")`",
        )

    )
}

data class AdditionalVariable(
    val name: String,
    val description: String,
)
