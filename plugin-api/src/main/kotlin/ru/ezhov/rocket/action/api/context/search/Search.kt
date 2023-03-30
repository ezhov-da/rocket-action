package ru.ezhov.rocket.action.api.context.search

/**
 * Поиск по действиям.
 *
 * Позволяет действию зарегистрировать себя для поиска
 */
interface Search {
    /**
     * Зарегистрировать действие
     *
     * @param id идентификатор действия
     * @param text текст по которому будет искаться действие
     */
    fun register(id: String, text: String)

    /**
     * Удалить зарегистрированное для поиска действие
     *
     * @param id идентификатор действия
     */
    fun delete(id: String)
}
