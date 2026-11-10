package ru.ezhov.rocket.action.application.chainaction.startup.infrastructure.model

import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions

data class JsonStartupActions(
    val stopOnError: Boolean = false,
    val startupActions: List<JsonStartupActionEntry> = emptyList(),
)

data class JsonStartupActionEntry(
    val id: String,
    val actionId: String,
    val input: String? = null,
    val enabled: Boolean = true,
)

internal fun JsonStartupActions.toStartupActions() = StartupActions(
    stopOnError = stopOnError,
    startupActions = startupActions.map {
        StartupActionEntry(
            id = it.id,
            actionId = it.actionId,
            input = it.input,
            enabled = it.enabled,
        )
    },
)

internal fun StartupActions.toJsonStartupActions() = JsonStartupActions(
    stopOnError = stopOnError,
    startupActions = startupActions.map {
        JsonStartupActionEntry(
            id = it.id,
            actionId = it.actionId,
            input = it.input,
            enabled = it.enabled,
        )
    },
)
