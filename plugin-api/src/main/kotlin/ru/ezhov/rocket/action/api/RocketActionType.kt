package ru.ezhov.rocket.action.api

/**
 * Тип действия
 */
fun interface RocketActionType {

    /**
     * Уникальный тип действия
     */
    fun value(): String
}