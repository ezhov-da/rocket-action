package ru.ezhov.rocket.action.plugin.jira.worklog.infrastructure

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import org.joda.time.DateTime
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeServiceException
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTask
import java.net.URI

class JiraCommitTimeService(
    private val username: String,
    private val password: String,
    private val url: URI,
) : CommitTimeService {
    override fun commit(task: CommitTimeTask): Either<CommitTimeServiceException, Unit> =
        try {
            client()
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


    private fun client() = AsynchronousJiraRestClientFactory()
        .createWithBasicHttpAuthentication(url, username, password)

}
