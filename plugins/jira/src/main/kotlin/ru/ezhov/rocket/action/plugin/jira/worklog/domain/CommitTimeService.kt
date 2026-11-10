package ru.ezhov.rocket.action.plugin.jira.worklog.domain

import arrow.core.Either
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTask

interface CommitTimeService {
    fun commit(task: CommitTimeTask): Either<CommitTimeServiceException, Unit>
}
