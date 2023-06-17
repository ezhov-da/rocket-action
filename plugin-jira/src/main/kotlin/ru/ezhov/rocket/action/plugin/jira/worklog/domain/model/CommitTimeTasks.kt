package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.Validator
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CommitTimeTasks private constructor(
    val commitTimeTask: List<CommitTimeTask> = emptyList(),
    val errors: List<CommitTimeTasksError> = emptyList(),
    private val delimiter: String,
) {
    companion object {
        fun of(
            value: String,
            delimiter: String,
            dateFormatPattern: String,
            constantsNowDate: List<String>,
            aliasForTaskIds: AliasForTaskIds,
            validator: Validator,
        ): CommitTimeTasks =
            if (value.isBlank()) {
                CommitTimeTasks(
                    errors = listOf(
                        CommitTimeTasksError("Data cannot be empty")
                    ),
                    delimiter = delimiter
                )
            } else {
                val rows = value.split("\n")
                if (rows.isEmpty()) {
                    CommitTimeTasks(errors = listOf(CommitTimeTasksError("No data")), delimiter = delimiter)
                } else {
                    rows
                        .map {
                            it.toCommitTimeTask(
                                delimiter = delimiter,
                                dateFormatPattern = dateFormatPattern,
                                constantsNowDate = constantsNowDate,
                                aliasForTaskIds = aliasForTaskIds,
                                validator = validator,
                            )
                        }
                        .let { tasks ->
                            val corrects = mutableListOf<CommitTimeTask?>()
                            val errors = mutableListOf<CommitTimeTasksError>()
                            tasks.forEach { t -> corrects.add(t.getOrHandle { errors.add(it); null }) }
                            CommitTimeTasks(
                                commitTimeTask = corrects.filterNotNull(),
                                errors = errors,
                                delimiter = delimiter,
                            )
                        }
                }
            }

        private fun String.toCommitTimeTask(
            delimiter: String,
            dateFormatPattern: String,
            constantsNowDate: List<String>,
            aliasForTaskIds: AliasForTaskIds,
            validator: Validator,
        ): Either<CommitTimeTasksError, CommitTimeTask> {
            val error by lazy { "For string '${this.trim()}' there are mistakes: " }
            return this
                .split(delimiter)
                .let { parts ->
                    if (parts.size != 4) {
                        CommitTimeTasksError(
                            "$error The data must consist of four columns separated by '$delimiter'. " +
                                "ID$delimiter'$dateFormatPattern' or '$constantsNowDate'" +
                                "${delimiter}Time in minutes${delimiter}Description"
                        ).left()
                    } else {
                        val errors = mutableListOf<String>()

                        val originalId = parts[0]
                        val idFinal = originalId.let { id -> aliasForTaskIds.taskIdByAlias(id) ?: id }
                        val dateFormat = DateTimeFormatter.ofPattern(dateFormatPattern)
                        val originalTimeAsString = parts[1]
                        val time = try {
                            when {
                                originalTimeAsString.isEmpty() -> LocalDateTime.now()
                                "^[+-]\\d+$".toRegex().matches(originalTimeAsString) -> {
                                    val symbol = originalTimeAsString.first()
                                    val value = originalTimeAsString.substring(1).toLong()
                                    when (symbol) {
                                        '+' -> LocalDateTime.now().plusDays(value)
                                        '-' -> LocalDateTime.now().minusDays(value)
                                        else -> LocalDateTime.now()
                                    }
                                }

                                constantsNowDate.contains(originalTimeAsString) -> LocalDateTime.now()
                                else -> LocalDateTime.parse(originalTimeAsString, dateFormat)
                            }
                        } catch (ex: DateTimeParseException) {
                            errors.add(
                                "Incorrect date and time format. " +
                                    "Correct " +
                                    "'$dateFormatPattern' or " +
                                    "'$constantsNowDate' or " +
                                    "'+/-days from the current date' or " +
                                    "'do not set a date'"
                            )
                            null
                        }
                        val timeSpentMinute = try {
                            parts[2].toInt()
                        } catch (ex: NumberFormatException) {
                            errors.add("Invalid time, must be a number. ")
                            null
                        }

                        // comment validation
                        val comment = parts[3]
                        val commentErrors = validator.validate(comment)
                        if (commentErrors.isNotEmpty()) {
                            errors.addAll(commentErrors)
                        }

                        if (errors.isNotEmpty()) {
                            CommitTimeTasksError("$error ${errors.joinToString(separator = "; ")}").left()
                        } else {
                            CommitTimeTask(
                                id = idFinal,
                                time = time!!,
                                timeSpentMinute = timeSpentMinute!!,
                                comment = comment,
                                originalId = originalId,
                                originalTime = originalTimeAsString,
                            ).right()
                        }
                    }
                }
        }
    }

    fun sumOfTimeTasksAsMinute() = commitTimeTask.sumOf { it.timeSpentMinute }

    fun sumOfTimeTasksAsHours(): Double =
        (sumOfTimeTasksAsMinute() / 60.toDouble())
            .toBigDecimal()
            .setScale(2, RoundingMode.CEILING)
            .toDouble()

    fun countOfTask() = commitTimeTask.size

    fun hasErrors() = errors.isNotEmpty()
}
