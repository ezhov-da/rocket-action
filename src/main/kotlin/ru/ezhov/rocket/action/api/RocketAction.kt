package ru.ezhov.rocket.action.api

import java.awt.Component

interface RocketAction {
    /**
     * True если действие подходит для поиска
     */
    fun contains(search: String): Boolean

    /**
     * Компонент для отображения
     */
    fun component(): Component
}
