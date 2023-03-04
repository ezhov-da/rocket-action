package ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations

enum class ValidationRule(
    val description: String
) {
    MIN_LENGTH("Минимальная длина"),
    MAX_LENGTH("Максимальная длина"),
}
