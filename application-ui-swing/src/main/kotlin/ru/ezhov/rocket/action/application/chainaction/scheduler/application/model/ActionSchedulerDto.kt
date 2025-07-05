package ru.ezhov.rocket.action.application.chainaction.scheduler.application.model

import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import java.io.File

data class ActionSchedulerDto(
    val action: Action,
    val actionScheduler: ActionScheduler,
    val logFile: File?,
)
