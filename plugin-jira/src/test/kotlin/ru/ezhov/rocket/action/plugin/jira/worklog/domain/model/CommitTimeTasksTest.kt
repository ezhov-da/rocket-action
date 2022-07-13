package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import org.junit.Test


internal class CommitTimeTasksTest {
    @Test
    fun test() {
        val commitTimeTasks = CommitTimeTasks.of(
            value = "какая-то задача_с_40_А это описание",
            delimiter = "_",
            dateFormatPattern = "yyyyMMddHHmm",
            constantsNowDate = listOf("now", "n", "с"),
            aliasForTaskIds = AliasForTaskIds.EMPTY
        )

        println(commitTimeTasks)
    }
}
