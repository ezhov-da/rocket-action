package ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model

import java.time.LocalDateTime

data class ActionSchedulers(
    val schedulers: List<ActionScheduler>
)

data class ActionScheduler(
    val actionId: String,
    var cron: String?,
    var dateOfTheLastLaunch: LocalDateTime?,
    var schedulerStatus: ActionSchedulerStatus
)

enum class ActionSchedulerStatus {
    NOT_LAUNCHED,
    SUCCESS,
    ERROR,
}
