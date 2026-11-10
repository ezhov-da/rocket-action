package ru.ezhov.rocket.action.application.chainaction.scheduler.application.model

/**
 * Thrown when the user-specified cron expression cannot be parsed by Quartz.
 *
 * [reason] contains the original parser message and is intended to be shown to the user.
 */
class InvalidCronExpressionException(
    val cron: String,
    val reason: String,
    cause: Throwable? = null,
) : IllegalArgumentException("Invalid cron expression '$cron': $reason", cause)
