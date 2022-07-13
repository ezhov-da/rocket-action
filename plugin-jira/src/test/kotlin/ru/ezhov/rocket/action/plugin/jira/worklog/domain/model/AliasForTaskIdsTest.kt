package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import org.junit.Test

class AliasForTaskIdsTest {
    @Test
    fun test() {
        val value = AliasForTaskIds.of("""
            Task-12_проc,col
            Task-11_123
            S-784_пв
            S-783_в
            F-104_о
        """.trimIndent())

        println(value)
        println(value.taskIdByAlias("пв"))
    }
}
