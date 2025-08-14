package ru.ezhov.rocket.action.plugin.jira.worklog.infrastructure

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.atlassian.jira.rest.client.api.JiraRestClient
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeTaskInfoException
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeTaskInfoRepository
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTaskInfo

class JiraCommitTimeTaskInfoRepository(
    private val jiraRestClient: JiraRestClient,
) : CommitTimeTaskInfoRepository {
    override fun info(id: String): Either<CommitTimeTaskInfoException, CommitTimeTaskInfo?> =
        try {
            jiraRestClient
                .let { client ->
                    client.issueClient.getIssue(id).claim()
                        ?.let { issue ->
                            CommitTimeTaskInfo(name = issue.summary)
                        }
                }
                .right()
        } catch (ex: Exception) {
            CommitTimeTaskInfoException(
                message = "Error get info for task with id='$id'",
                cause = ex
            ).left()
        }
}
