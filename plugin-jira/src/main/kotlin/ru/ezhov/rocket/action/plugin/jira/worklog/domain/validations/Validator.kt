package ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations

interface Validator {
    fun validate(source: String): List<String>
}
