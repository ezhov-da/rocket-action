package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import org.junit.Test


internal class CommitTimeTasksTest {
    @Test
    fun test() {
        val сommitTimeTasks = CommitTimeTasks.of("qweqweqwe")

        println(сommitTimeTasks)
    }
}
