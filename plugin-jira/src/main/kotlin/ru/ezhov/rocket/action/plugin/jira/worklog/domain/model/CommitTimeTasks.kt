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
    private val delimiter: String,
) {
    companion object {
        fun of(
            value: String,
            delimiter: String,
            dateFormatPattern: String,
            constantsNowDate: List<String>,
            aliasForTaskIds: AliasForTaskIds,
        ): CommitTimeTasks =
            if (value.isBlank()) {
                CommitTimeTasks(
                    errors = listOf(
                        CommitTimeTasksError("Данные не могут быть пустыми")
                    ),
                    delimiter = delimiter
                )
            } else {
                val rows = value.split("\n")
                if (rows.isEmpty()) {
                    CommitTimeTasks(errors = listOf(CommitTimeTasksError("Нет данных")), delimiter = delimiter)
                } else {
                    rows
                        .map {
                            it.toCommitTimeTask(
                                delimiter = delimiter,
                                dateFormatPattern = dateFormatPattern,
                                constantsNowDate = constantsNowDate,
                                aliasForTaskIds = aliasForTaskIds,
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
        ): Either<CommitTimeTasksError, CommitTimeTask> {
            val error by lazy { "Для строки '${this.trim()}' есть ошибки: " }
            return this
                .split(delimiter)
                .let { parts ->
                    if (parts.size != 4) {
                        CommitTimeTasksError(
                            "$error Данные должны состоять из четырёх столбцов разделённых '$delimiter'. " +
                                "ID$delimiter'$dateFormatPattern' или '$constantsNowDate'" +
                                "${delimiter}Время в минутах${delimiter}Описание"
                        ).left()
                    } else {
                        val errors = mutableListOf<String>()

                        val originalId = parts[0]
                        val idFinal = originalId.let { id -> aliasForTaskIds.taskIdByAlias(id) ?: id }
                        val dateFormat = DateTimeFormatter.ofPattern(dateFormatPattern)
                        val originalTimeAsString = parts[1]
                        val time = try {
                            when {
                                constantsNowDate.contains(originalTimeAsString) -> LocalDateTime.now()
                                else -> LocalDateTime.parse(originalTimeAsString, dateFormat)
                            }
                        } catch (ex: DateTimeParseException) {
                            errors.add("Некорректный формат даты и времени. Корректный '$dateFormatPattern' или '$constantsNowDate'")
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
