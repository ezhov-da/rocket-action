package ru.ezhov.rocket.action.plugin.jira.worklog.infrastructure

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput
import org.joda.time.DateTime
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeServiceException
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTask

class JiraCommitTimeService(
    private val jiraRestClient: JiraRestClient,
) : CommitTimeService {
    override fun commit(task: CommitTimeTask): Either<CommitTimeServiceException, Unit> =
        try {
            jiraRestClient
                .let { client ->
                    client.issueClient.getIssue(task.id).claim()?.let { issue ->
                        client.issueClient.addWorklog(
                            issue.worklogUri,
                            WorklogInput.create(
                                issue.self,
                                task.comment,
                                task.time.let { time ->
                                    DateTime(
                                        time.year,
                                        time.monthValue,
                                        time.dayOfMonth,
                                        time.hour,
                                        time.minute,
                                    )
                                },
                                task.timeSpentMinute,
                            )
                        ).claim()
                    }
                }
            Unit.right()
        } catch (ex: Exception) {
            CommitTimeServiceException(
                message = "Error jira work log. Part of information not add.",
                cause = ex
            ).left()
        }
}
