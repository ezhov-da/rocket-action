package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import java.time.LocalDateTime

data class CommitTimeTask constructor(
    val id: String,
    val time: LocalDateTime,
    val timeSpentMinute: Int,
    val comment: String,
)
