package ru.ezhov.rocket.action.application.chainaction.scheduler.application

import org.quartz.CronExpression
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.InvalidCronExpressionException

/**
 * Single place for cron validation, so the UI and the scheduler runner
 * report the same reason for an invalid expression.
 */
object CronExpressionValidator {
    /**
     * @throws InvalidCronExpressionException if [cron] is not a valid Quartz cron expression.
     */
    fun validate(cron: String) {
        try {
            CronExpression.validateExpression(cron)
        } catch (e: Exception) {
            throw InvalidCronExpressionException(
                cron = cron,
                reason = e.message ?: "cron expression could not be parsed",
                cause = e,
            )
        }
    }
}
