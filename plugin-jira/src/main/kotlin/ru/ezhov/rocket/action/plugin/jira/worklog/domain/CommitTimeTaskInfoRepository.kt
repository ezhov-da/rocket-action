package ru.ezhov.rocket.action.plugin.jira.worklog.domain

import arrow.core.Either
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTaskInfo

interface CommitTimeTaskInfoRepository {
    fun info(id: String): Either<CommitTimeTaskInfoException, CommitTimeTaskInfo?>
}
