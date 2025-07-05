package ru.ezhov.rocket.action.application.chainaction.scheduler.domain

import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import java.io.File
import java.time.LocalDateTime

interface ActionSchedulerLogRepository {
    fun get(actionId: String): File?

    fun save(actionId: String, date: LocalDateTime, status: ActionSchedulerStatus, result: String?, ex: Exception?)

    fun delete(actionId: String)
}
