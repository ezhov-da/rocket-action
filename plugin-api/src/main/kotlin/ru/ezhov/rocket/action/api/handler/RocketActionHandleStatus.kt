package ru.ezhov.rocket.action.api.handler

/**
 * Processor response
 */
sealed class RocketActionHandleStatus {
    /**
     * Successful processing.
     *
     * [values] - execution result
     *
     */
    class Success(
        val values: Map<String, String?> = emptyMap()
    ) : RocketActionHandleStatus()

    /**
     * Invalid input parameters.
     *
     * [errors] - error list
     */
    class InvalidInputData(val errors: List<String>) : RocketActionHandleStatus()

    /**
     * Error.
     *
     * [message] - error message
     * [cause] - error reason
     */
    class Error(val message: String, val cause: Exception? = null) : RocketActionHandleStatus()
}
