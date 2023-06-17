package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.Validator

internal class CommitTimeTasksTest {
    @Test
    fun test() {
        val commitTimeTasks = CommitTimeTasks.of(
            value = "some task_s_40_A this description",
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
