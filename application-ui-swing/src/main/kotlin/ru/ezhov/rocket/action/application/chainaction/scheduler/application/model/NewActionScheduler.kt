package ru.ezhov.rocket.action.application.chainaction.scheduler.application.model

data class CreateOrUpdateActionScheduler(
    val actionId: String,
    val cron: String?,
)
