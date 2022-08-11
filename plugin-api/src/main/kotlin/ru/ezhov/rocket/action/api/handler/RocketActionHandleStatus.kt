package ru.ezhov.rocket.action.api.handler

import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey

/**
 * Ответ обработчика
 */
sealed class RocketActionHandleStatus {
    /**
     * Успешная обработка.
     *
     * [values] - результат выполнения
     *
     */
    class Success(
        val values: Map<RocketActionConfigurationPropertyKey, String?> = emptyMap()
    ) : RocketActionHandleStatus()

    /**
     * Некорректные входные параметры.
     *
     * [errors] - список ошибок
     */
    class InvalidInputData(val errors: List<String>) : RocketActionHandleStatus()

    /**
     * Ошибка.
     *
     * [message] - ошибка
     * [cause] - причина ошибки
     */
    class Error(val message: String, val cause: Exception? = null) : RocketActionHandleStatus()
}
