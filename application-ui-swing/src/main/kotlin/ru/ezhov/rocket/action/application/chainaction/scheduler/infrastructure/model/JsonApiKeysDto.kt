package ru.ezhov.rocket.action.application.chainaction.scheduler.infrastructure.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonActionSchedulers(
    val schedulers: List<JsonActionScheduler>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonActionScheduler(
    val actionId: String,
    var cron: String?,
    var dateOfTheLastLaunch: LocalDateTime?,
    var schedulerStatus: ActionSchedulerStatus
)
