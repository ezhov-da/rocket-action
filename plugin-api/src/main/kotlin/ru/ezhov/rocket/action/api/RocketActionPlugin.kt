package ru.ezhov.rocket.action.api

/**
 * Плагин действия
 */
interface RocketActionPlugin {
    /**
     * Фабрика по созданию UI
     */
    fun factory(): RocketActionFactoryUi

    /**
     * Конфигурация действия
     */
    fun configuration(): RocketActionConfiguration
}
