package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.Validator

internal class CommitTimeTasksTest {
    @Test
    fun test() {
        val commitTimeTasks = CommitTimeTasks.of(
            value = "какая-то задача_с_40_А это описание",
            delimiter = "_",
            dateFormatPattern = "yyyyMMddHHmm",
            constantsNowDate = listOf("now", "n", "с"),
            aliasForTaskIds = AliasForTaskIds.EMPTY,
            validator = object : Validator {
                override fun validate(source: String): List<String> = listOf("Error")

            }
        )

        println(commitTimeTasks)
    }
}
