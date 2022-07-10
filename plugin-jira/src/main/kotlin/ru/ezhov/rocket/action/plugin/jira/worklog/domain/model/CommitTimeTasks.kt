package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CommitTimeTasks private constructor(
    val commitTimeTask: List<CommitTimeTask> = emptyList(),
    val errors: List<CommitTimeTasksError> = emptyList(),
) {
    companion object {
        fun of(value: String): CommitTimeTasks =
            if (value.isBlank()) {
                CommitTimeTasks(errors = listOf(CommitTimeTasksError("Данные не могут быть пустыми")))
            } else {
                val rows = value.split("\n")
                if (rows.isEmpty()) {
                    CommitTimeTasks(errors = listOf(CommitTimeTasksError("Нет данных")))
                } else {
                    rows
                        .map { it.toCommitTimeTask() }
                        .let { tasks ->
                            val corrects = mutableListOf<CommitTimeTask?>()
                            val errors = mutableListOf<CommitTimeTasksError>()
                            tasks.forEach { t -> corrects.add(t.getOrHandle { errors.add(it); null }) }
                            CommitTimeTasks(
                                commitTimeTask = corrects.filterNotNull(),
                                errors = errors
                            )
                        }
                }
            }

        private fun String.toCommitTimeTask(): Either<CommitTimeTasksError, CommitTimeTask> {
            val error by lazy { "Для строки '$this' есть ошибки: " }
            return this
                .split("___")
                .let { parts ->
                    if (parts.size != 4) {
                        CommitTimeTasksError("$error Данные должны состоять из четырёх столбцов разделённых '___'. ").left()
                    } else {
                        val errors = mutableListOf<String>()

                        val id = parts[0]
                        val dateFormatPattern = "yyyyMMddHHmm"
                        val dateFormat = DateTimeFormatter.ofPattern(dateFormatPattern)
                        val time = try {
                            when (val timeAsString = parts[1]) {
                                "now" -> LocalDateTime.now()
                                else -> LocalDateTime.parse(timeAsString, dateFormat)
                            }
                        } catch (ex: DateTimeParseException) {
                            errors.add("Некорректный формат даты и времени. Корректный '$dateFormatPattern' или 'now'")
                            null
                        }
                        val timeSpentMinute = try {
                            parts[2].toInt()
                        } catch (ex: NumberFormatException) {
                            errors.add("Некорректное время, должно быть число. ")
                            null
                        }
                        val comment = parts[3]
                        if (errors.isNotEmpty()) {
                            CommitTimeTasksError("$error ${errors.joinToString(separator = "; ")}").left()
                        } else {
                            CommitTimeTask(
                                id = id,
                                time = time!!,
                                timeSpentMinute = timeSpentMinute!!,
                                comment = comment,
                            ).right()
                        }
                    }
                }
        }
    }

    fun sumOfTimeTasksAsMinute() = commitTimeTask.sumOf { it.timeSpentMinute }

    fun sumOfTimeTasksAsHours(): Double =
        (sumOfTimeTasksAsMinute() / 60)
            .toBigDecimal()
            .setScale(1, RoundingMode.CEILING)
            .toDouble()

    fun countOfTask() = commitTimeTask.size

    fun hasErrors() = errors.isNotEmpty()
}
