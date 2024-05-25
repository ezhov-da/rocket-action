package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.Validator

internal class CommitTimeTasksTest {
    @Test
    fun test() {
        val commitTimeTasks = CommitTimeTasks.of(
            value = "some task_n_40_A this description description_with_symbol '_'",
            delimiter = "_",
            dateFormatPattern = "yyyyMMddHHmm",
            constantsNowDate = listOf("now", "n", "с"),
            aliasForTaskIds = AliasForTaskIds.EMPTY,
            validator = object : Validator {
                override fun validate(source: String): List<String> = emptyList() // listOf("Error")
            }
        )

        println(commitTimeTasks)

        with(commitTimeTasks) {
            assertThat(commitTimeTask).hasSize(1)
            assertThat(commitTimeTask[0].id).isEqualTo("some task")
            assertThat(commitTimeTask[0].timeSpentMinute).isEqualTo(40)
            assertThat(commitTimeTask[0].comment).isEqualTo("A this description description_with_symbol '_'")
        }
    }
}
