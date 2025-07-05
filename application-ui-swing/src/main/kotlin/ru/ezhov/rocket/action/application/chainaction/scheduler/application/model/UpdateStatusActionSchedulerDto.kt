package ru.ezhov.rocket.action.application.chainaction.scheduler.application.model

import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus

data class UpdateStatusActionSchedulerDto(
    val actionId: String,
    val status: UpdateStatusInfoActionSchedulerDto
)

sealed class UpdateStatusInfoActionSchedulerDto(
    val status: ActionSchedulerStatus
) {
    class Error(
        val ex: Exception,
        status: ActionSchedulerStatus = ActionSchedulerStatus.ERROR
    ) : UpdateStatusInfoActionSchedulerDto(status)

    class Success(
        val result: String?,
        status: ActionSchedulerStatus = ActionSchedulerStatus.SUCCESS
    ) : UpdateStatusInfoActionSchedulerDto(status)
}
