package ru.ezhov.rocket.action.plugin.jira.worklog.infrastructure

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import java.net.URI

object JiraRestClientBuilder {
    fun build(
        username: String,
        password: String,
        cookie: String,
        url: URI,
    ): JiraRestClient =
        when {
            username.isNotEmpty() -> AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(url, username, password)

            cookie.isNotEmpty() -> AsynchronousJiraRestClientFactory()
                .createWithAuthenticationHandler(url) { builder -> builder.setHeader("Cookie", cookie) }

            else -> throw RuntimeException("Not set username and password or auth cookie")
        }
}
