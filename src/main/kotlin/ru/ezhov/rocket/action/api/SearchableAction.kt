package ru.ezhov.rocket.action.api

interface SearchableAction {
    /**
     * True если действие подходит для поиска
     */
    fun contains(search: String): Boolean
}